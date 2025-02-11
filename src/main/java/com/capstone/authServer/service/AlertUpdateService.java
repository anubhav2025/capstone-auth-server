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
import com.capstone.authServer.model.Credential;
import com.capstone.authServer.repository.CredentialRepository;

@Service
public class AlertUpdateService {

    private final CredentialRepository credentialRepository;
    private final WebClient.Builder webClientBuilder;
    private final ScanEventProducerService scanEventProducerService; 

    public AlertUpdateService(CredentialRepository credentialRepository,
                              WebClient.Builder webClientBuilder,
                              ScanEventProducerService scanEventProducerService) {
        this.credentialRepository = credentialRepository;
        this.webClientBuilder = webClientBuilder;
        this.scanEventProducerService = scanEventProducerService;
    }

    public void updateAlertState(AlertUpdateDTO dto) throws Exception {
        // 1) Find the credential for (owner, repo)
        Credential cred = credentialRepository.findByOwnerAndRepository(dto.getOwner(), dto.getRepo());
        if (cred == null) {
            throw new RuntimeException("No credentials found for " + dto.getOwner() + "/" + dto.getRepo());
        }
        String token = cred.getPersonalAccessToken();

        // 2) Build the GH endpoint depending on the toolType
        String baseUrl = "https://api.github.com/repos/" + dto.getOwner() + "/" + dto.getRepo();
        String patchUrl;
        // If toolType is "CODE_SCAN","DEPENDABOT","SECRET_SCAN"
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

        // 3) Prepare request body
        // We must map internal values (e.g. "DISMISSED","FALSE_POSITIVE") -> 
        // GitHub-accepted strings ("dismissed","false positive").
        // We'll store them in a map:
        Map<String, Object> patchRequest = new HashMap<>();

        // Let's do a small function to map reason => GitHub string
        String ghReason = mapReason(toolTypeEnum, dto.getReason());

        // For SECRET_SCAN: "resolved"/"open" and "resolution"
        // For CODE_SCAN/DEPENDABOT: "dismissed"/"open" and "dismissed_reason"
        // We handle "open" or "dismissed"/"resolved" logic:

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
        ScanEventDTO eventDTO = new ScanEventDTO();
        eventDTO.setOwner(cred.getOwner());
        eventDTO.setRepository(cred.getRepository());
        eventDTO.setUsername(cred.getOwner());
        eventDTO.setTools(Arrays.asList(ScanType.ALL));

        scanEventProducerService.publishScanEvent(eventDTO);
    }

    /**
     * Map internal reason (e.g. "FALSE_POSITIVE") to 
     * GitHub accepted strings (e.g. "false positive").
     */
    private String mapReason(ScanToolType toolType, String reason) {
        if (reason == null) return null;

        // Example. Adjust as needed:
        // CODE SCAN => "false positive","won't fix","used in tests"
        // DEPENDABOT => "fix_started","inaccurate","no_bandwidth","not_used","tolerable_risk"
        // SECRET SCAN => "false_positive","won't fix","revoked","used_in_tests"
        // We'll do a couple of examples:

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
