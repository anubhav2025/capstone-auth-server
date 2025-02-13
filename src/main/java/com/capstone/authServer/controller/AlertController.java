package com.capstone.authServer.controller;

import com.capstone.authServer.dto.AlertUpdateDTO;
import com.capstone.authServer.security.RoleGuard;
import com.capstone.authServer.service.AlertUpdateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/alert")
public class AlertController {

    private final AlertUpdateService alertUpdateService;

    public AlertController(AlertUpdateService alertUpdateService) {
        this.alertUpdateService = alertUpdateService;
    }

    @PostMapping("/updateState")
    @RoleGuard(allowed={"SUPER_ADMIN"})
    public ResponseEntity<?> updateAlertState(@RequestParam("tenantId") String tenantId, @RequestBody AlertUpdateDTO request) {
        // tenantId is in the DTO; RoleGuardAspect uses it to check roles
        try {
            alertUpdateService.updateAlertState(request);
            Map<String, String> response = new HashMap<>();
            response.put("message", "State update triggered successfully.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to update state: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
