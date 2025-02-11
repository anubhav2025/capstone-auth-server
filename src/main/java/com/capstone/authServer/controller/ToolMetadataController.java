package com.capstone.authServer.controller;

import com.capstone.authServer.enums.*;
import com.capstone.authServer.security.RoleGuard;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class ToolMetadataController {

    /**
     * Returns all possible states and dismiss reasons for each tool.
     * The React front-end can call this to avoid hard-coding.
     */
    @GetMapping("/alert/states-reasons")
    @RoleGuard(allowed={"SUPER_ADMIN","ADMIN", "USER"})
    public Map<String, Object> getStatesAndReasons() {
        Map<String, Object> response = new LinkedHashMap<>();

        // CODE_SCAN
        Map<String, Object> codeScan = new LinkedHashMap<>();
        codeScan.put("states", CodeScanState.values());
        codeScan.put("dismissedReasons", CodeScanDismissedReason.values());

        // DEPENDABOT
        Map<String, Object> dependabot = new LinkedHashMap<>();
        dependabot.put("states", DependabotState.values());
        dependabot.put("dismissedReasons", DependabotDismissedReason.values());

        // SECRET_SCAN
        Map<String, Object> secretScan = new LinkedHashMap<>();
        secretScan.put("states", SecretScanState.values());
        secretScan.put("resolvedReasons", SecretScanResolvedReason.values());

        response.put("CODE_SCAN", codeScan);
        response.put("DEPENDABOT", dependabot);
        response.put("SECRET_SCAN", secretScan);

        return response;
    }
}
