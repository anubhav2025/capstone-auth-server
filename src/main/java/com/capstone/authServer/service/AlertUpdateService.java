package com.capstone.authServer.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.capstone.authServer.dto.AlertUpdateDTO;
import com.capstone.authServer.dto.ScanEventDTO;
import com.capstone.authServer.dto.ScanToolType;
import com.capstone.authServer.dto.ScanType;
import com.capstone.authServer.model.Tenant;
import com.capstone.authServer.repository.TenantRepository;

@Service
public class AlertUpdateService {

    private final TenantRepository tenantRepository;
    private final WebClient.Builder webClientBuilder;
    private final ScanEventProducerService scanEventProducerService;

    public AlertUpdateService(TenantRepository tenantRepository,
                              WebClient.Builder webClientBuilder,
                              ScanEventProducerService scanEventProducerService) {
        this.tenantRepository = tenantRepository;
        this.webClientBuilder = webClientBuilder;
        this.scanEventProducerService = scanEventProducerService;
    }

    public void updateAlertState(AlertUpdateDTO dto) throws Exception {
        // 1) Find the tenant by tenantId
        Tenant tenant = tenantRepository.findByTenantId(dto.getTenantId());
        if (tenant == null) {
            throw new RuntimeException("No tenant found for tenantId: " + dto.getTenantId());
        }

        String token = tenant.getPat();

        // 2) Build the GitHub endpoint depending on the toolType
        //    (We derive owner/repo from Tenant)
        String baseUrl = "https://api.github.com/repos/" + tenant.getOwner() + "/" + tenant.getRepo();
        String patchUrl;

        ScanToolType toolTypeEnum = ScanToolType.valueOf(dto.getToolType());
        switch (toolTypeEnum) {
            case CODE_SCAN:
                patchUrl = baseUrl + "/code-scanning/alerts/" + dto.getAlertNumber();
                break;
            case DEPENDABOT:
                patchUrl = baseUrl + "/dependabot/alerts/" + dto.getAlertNumber();
                break;
            case SECRET_SCAN:
                patchUrl = baseUrl + "/secret-scanning/alerts/" + dto.getAlertNumber();
                break;
            default:
                throw new IllegalStateException("Unknown tool type: " + dto.getToolType());
        }

        // 3) Prepare request body, including the mapping logic for reasons
        //    (same as your original code)
        Map<String, Object> patchRequest = new HashMap<>();

        // Map the internal reason -> GitHub-accepted strings
        String ghReason = mapReason(toolTypeEnum, dto.getReason());

        // For SECRET_SCAN: use "resolved"/"open" + "resolution"
        // For CODE_SCAN/DEPENDABOT: use "dismissed"/"open" + "dismissed_reason"
        String newState = dto.getNewState().toLowerCase(); 
        // e.g. "DISMISSED" -> "dismissed", "OPEN" -> "open", "RESOLVED" -> "resolved"

        if (toolTypeEnum == ScanToolType.SECRET_SCAN) {
            if ("resolved".equalsIgnoreCase(newState)) {
                patchRequest.put("state", "resolved");
                if (ghReason != null) {
                    patchRequest.put("resolution", ghReason);
                }
            } else {
                // fallback => open
                patchRequest.put("state", "open");
            }
        } else {
            // CODE_SCAN or DEPENDABOT
            if ("dismissed".equalsIgnoreCase(newState)) {
                patchRequest.put("state", "dismissed");
                if (ghReason != null) {
                    patchRequest.put("dismissed_reason", ghReason);
                }
            } else {
                // fallback => open
                patchRequest.put("state", "open");
            }
        }

        WebClient webClient = webClientBuilder.build();
        System.out.println("PATCH URL: " + patchUrl);
        System.out.println("PATCH BODY: " + patchRequest);

        // 4) Perform the PATCH request
        webClient.patch()
                 .uri(patchUrl)
                 .header("Authorization", "Bearer " + token)
                 .header("Accept", "application/vnd.github+json")
                 .bodyValue(patchRequest)
                 .retrieve()
                 .bodyToMono(String.class)
                 .block(); // simplistic approach

        // 5) After success, re-trigger scanning
        //    We now use tenantId + tools in ScanEventDTO, not owner/repo
        ScanEventDTO eventDTO = new ScanEventDTO();
        eventDTO.setTenantId(dto.getTenantId());
        eventDTO.setTools(Arrays.asList(ScanType.ALL));

        scanEventProducerService.publishScanEvent(eventDTO);
    }

    /**
     * Map internal reason (e.g. "FALSE_POSITIVE") -> GitHub accepted strings 
     * (e.g. "false positive").
     */
    private String mapReason(ScanToolType toolType, String reason) {
        if (reason == null) return null;

        switch (toolType) {
            case CODE_SCAN:
                switch (reason.toUpperCase()) {
                    case "FALSE_POSITIVE": return "false positive";
                    case "WONT_FIX":       return "won't fix";
                    case "USED_IN_TESTS":  return "used in tests";
                    default: return null;
                }
            case DEPENDABOT:
                switch (reason.toUpperCase()) {
                    case "FIX_STARTED":   return "fix_started";
                    case "INACCURATE":    return "inaccurate";
                    case "NO_BANDWIDTH":  return "no_bandwidth";
                    case "NOT_USED":      return "not_used";
                    case "TOLERABLE_RISK":return "tolerable_risk";
                    default: return null;
                }
            case SECRET_SCAN:
                switch (reason.toUpperCase()) {
                    case "FALSE_POSITIVE": return "false_positive";
                    case "WONT_FIX":       return "won't fix"; 
                    case "REVOKED":        return "revoked";
                    case "USED_IN_TESTS":  return "used_in_tests";
                    default: return null;
                }
            default:
                return null;
        }
    }
}
