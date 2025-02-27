package com.capstone.authServer.service;

import com.capstone.authServer.dto.ticketing.TicketResponseDTO;
import com.capstone.authServer.model.Finding;
import com.capstone.authServer.model.Tenant;
import com.capstone.authServer.model.TenantTicket;
import com.capstone.authServer.repository.TenantRepository;
import com.capstone.authServer.repository.TenantTicketRepository;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class JiraTicketService {

    private final TenantRepository tenantRepository;
    private final TenantTicketRepository tenantTicketRepository;
    private final ElasticSearchService elasticSearchService;
    private final WebClient.Builder webClientBuilder;

    public JiraTicketService(TenantRepository tenantRepository,
                             TenantTicketRepository tenantTicketRepository,
                             ElasticSearchService elasticSearchService,
                             WebClient.Builder webClientBuilder) {
        this.tenantRepository = tenantRepository;
        this.tenantTicketRepository = tenantTicketRepository;
        this.elasticSearchService = elasticSearchService;
        this.webClientBuilder = webClientBuilder;
    }

    /**
     * Create a JIRA ticket and store references in ES and DB.
     */
    public String createTicket(String tenantId, String findingId, String summary, String description) {
        Tenant tenant = getTenantOrThrow(tenantId);

        // 1) Build request body for JIRA
        Map<String, Object> fieldsMap = new HashMap<>();
        Map<String, Object> projectMap = new HashMap<>();
        projectMap.put("key", tenant.getProjectKey()); // e.g. "CRM"

        Map<String, Object> issueTypeMap = new HashMap<>();
        issueTypeMap.put("name", "Bug"); // or "Task", etc.

        fieldsMap.put("project", projectMap);
        fieldsMap.put("summary", summary);
        fieldsMap.put("description", description);
        fieldsMap.put("issuetype", issueTypeMap);

        Map<String, Object> payload = new HashMap<>();
        payload.put("fields", fieldsMap);

        // 2) POST to JIRA
        String jiraCreateUrl = "https://" + tenant.getAccountUrl() + "/rest/api/2/issue";
        
        Map<String, Object> responseBody = webClientBuilder.build()
            .post()
            .uri(jiraCreateUrl)
            .header(HttpHeaders.AUTHORIZATION, buildBasicAuthHeader(tenant.getEmail(), tenant.getApiToken()))
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(payload)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
            .block();

        if (responseBody == null) {
            throw new RuntimeException("Failed to create JIRA issue. Empty response.");
        }

        // 3) Get the newly created JIRA key
        String ticketKey = (String) responseBody.get("key"); // e.g. "CRM-11"

        // 4) Update the ES Finding with this ticketId
        Finding finding = retrieveSingleFinding(tenantId, findingId);
        finding.setTicketId(ticketKey);
        elasticSearchService.saveFinding(tenantId, finding);

        // 5) Create TenantTicket mapping in DB
        TenantTicket tenantTicket = new TenantTicket(tenantId, ticketKey, findingId);
        tenantTicketRepository.save(tenantTicket);

        return ticketKey;
    }

    /**
     * Retrieve all JIRA issues for the given tenant
     * by looking up TenantTicket and then fetching each ticket from JIRA.
     */
    public List<TicketResponseDTO> getAllTicketsForTenant(String tenantId) {
        Tenant tenant = getTenantOrThrow(tenantId);
        List<TenantTicket> tenantTickets = tenantTicketRepository.findAllByTenantId(tenantId);
        if (tenantTickets.isEmpty()) {
            return Collections.emptyList();
        }

        List<TicketResponseDTO> result = new ArrayList<>();
        for (TenantTicket tt : tenantTickets) {
            TicketResponseDTO dto = fetchTicketFromJira(tenant, tt.getTicketId());
            if (dto != null) {
                // UPDATED: we know the findingId from tenantTicket
                dto.setFindingId(tt.getEsFindingId());
                result.add(dto);
            }
        }
        return result;
    }

    /**
     * Return a single ticket by ID, also attach the associated findingId
     */
    public TicketResponseDTO getTicketById(String tenantId, String ticketId) {
        Tenant tenant = getTenantOrThrow(tenantId);

        // 1) fetch ticket from JIRA
        String url = "https://" + tenant.getAccountUrl() + "/rest/api/2/issue/" + ticketId;

        Map<String, Object> body = webClientBuilder.build()
            .get()
            .uri(url)
            .header(HttpHeaders.AUTHORIZATION, buildBasicAuthHeader(tenant.getEmail(), tenant.getApiToken()))
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
            .block();

        if (body == null) {
            throw new RuntimeException("Empty response from JIRA for ticketId=" + ticketId);
        }

        // 2) parse
        String key = (String) body.get("key");
        @SuppressWarnings("unchecked")
        Map<String, Object> fields = (Map<String, Object>) body.get("fields");
        if (fields == null) {
            throw new RuntimeException("No fields found in JIRA response for ticketId=" + ticketId);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> issueTypeMap = (Map<String, Object>) fields.get("issuetype");
        String issueTypeName = (String) issueTypeMap.get("name");
        String issueTypeDesc = (String) issueTypeMap.get("description");

        String summary = (String) fields.get("summary");
        @SuppressWarnings("unchecked")
        Map<String, Object> statusMap = (Map<String, Object>) fields.get("status");
        String statusName = (statusMap != null) ? (String) statusMap.get("name") : null;

        TicketResponseDTO dto = new TicketResponseDTO(key, issueTypeName, issueTypeDesc, summary, statusName);

        // UPDATED: retrieve findingId from TenantTicket
        tenantTicketRepository.findByTicketId(ticketId).ifPresent(tt -> {
            dto.setFindingId(tt.getEsFindingId());
        });

        return dto;
    }

    /**
     * Update ticket status (To Do -> Done), applying transitions in a loop until none remain.
     */
    public void updateTicketStatusToDone(String tenantId, String ticketId) {
        Tenant tenant = getTenantOrThrow(tenantId);

        while (true) {
            // 1) fetch transitions
            String transitionsUrl = "https://" + tenant.getAccountUrl() 
                + "/rest/api/2/issue/" + ticketId
                + "/transitions?expand=transitions.fields";

            Map<String, Object> transitionsBody = webClientBuilder.build()
                .get()
                .uri(transitionsUrl)
                .header(HttpHeaders.AUTHORIZATION, buildBasicAuthHeader(tenant.getEmail(), tenant.getApiToken()))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

            if (transitionsBody == null) {
                throw new RuntimeException("Failed to fetch transitions for ticketId=" + ticketId);
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> transitions =
                (List<Map<String, Object>>) transitionsBody.get("transitions");

            // if no transitions => final state
            if (transitions == null || transitions.isEmpty()) {
                break;
            }

            // take the first transition
            Map<String, Object> firstTransition = transitions.get(0);
            String transitionId = (String) firstTransition.get("id");
            if (transitionId == null) {
                throw new RuntimeException("No 'id' in transition object: " + firstTransition);
            }

            // apply this transition
            String transitionsPostUrl = "https://" + tenant.getAccountUrl()
                + "/rest/api/2/issue/" + ticketId + "/transitions";

            Map<String, Object> updatePayload = new HashMap<>();
            Map<String, Object> transitionObj = new HashMap<>();
            transitionObj.put("id", transitionId);
            updatePayload.put("transition", transitionObj);

            webClientBuilder.build()
                .post()
                .uri(transitionsPostUrl)
                .header(HttpHeaders.AUTHORIZATION, buildBasicAuthHeader(tenant.getEmail(), tenant.getApiToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(updatePayload)
                .retrieve()
                .toBodilessEntity()
                .block();
            // loop again to see if more transitions remain
        }
    }

    // private helpers

    private Tenant getTenantOrThrow(String tenantId) {
        Tenant tenant = tenantRepository.findByTenantId(tenantId);
        if (tenant == null) {
            throw new RuntimeException("Tenant not found for tenantId=" + tenantId);
        }
        return tenant;
    }

    private String buildBasicAuthHeader(String email, String apiToken) {
        String auth = email + ":" + apiToken;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
        return "Basic " + new String(encodedAuth);
    }

    private TicketResponseDTO fetchTicketFromJira(Tenant tenant, String ticketId) {
        String url = "https://" + tenant.getAccountUrl() + "/rest/api/2/issue/" + ticketId;

        Map<String, Object> body = webClientBuilder.build()
            .get()
            .uri(url)
            .header(HttpHeaders.AUTHORIZATION, buildBasicAuthHeader(tenant.getEmail(), tenant.getApiToken()))
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
            .block();

        if (body == null) {
            return null;
        }

        String key = (String) body.get("key");
        Map<String, Object> fields = (Map<String, Object>) body.get("fields");
        if (fields == null) {
            return null;
        }

        Map<String, Object> issueTypeMap = (Map<String, Object>) fields.get("issuetype");
        String issueTypeName = (String) issueTypeMap.get("name");
        String issueTypeDesc = (String) issueTypeMap.get("description");

        String summary = (String) fields.get("summary");
        Map<String, Object> statusMap = (Map<String, Object>) fields.get("status");
        String statusName = statusMap != null ? (String) statusMap.get("name") : null;

        TicketResponseDTO dto = new TicketResponseDTO(key, issueTypeName, issueTypeDesc, summary, statusName);

        // UPDATED: attach findingId from DB
        tenantTicketRepository.findByTicketId(ticketId).ifPresent(tt -> {
            dto.setFindingId(tt.getEsFindingId());
        });

        return dto;
    }

    private Finding retrieveSingleFinding(String tenantId, String findingId) {
        List<Finding> results = elasticSearchService.searchFindingsById(tenantId, findingId);
        if (results.isEmpty()) {
            throw new RuntimeException("Finding not found in ES. ID=" + findingId);
        }
        return results.get(0);
    }
}
