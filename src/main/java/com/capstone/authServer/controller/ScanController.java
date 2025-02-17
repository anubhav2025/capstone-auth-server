package com.capstone.authServer.controller;

import com.capstone.authServer.enums.ToolTypes;
import com.capstone.authServer.security.RoleGuard;
import com.capstone.authServer.service.ScanEventProducerService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/scan")
public class ScanController {

    private final ScanEventProducerService producerService;

    @Autowired
    public ScanController(ScanEventProducerService producerService) {
        this.producerService = producerService;
    }

    @PostMapping("/publish")
    @RoleGuard(allowed = {"SUPER_ADMIN", "ADMIN"})
    public Map<String, String> publishScan(
            @RequestParam("tenantId") String tenantId,
            @RequestBody List<String> tools // e.g. ["ALL"] or ["CODE_SCAN", "DEPENDABOT"]
    ) {
        // Determine the final list of tools to process
        List<ToolTypes> finalTools = new ArrayList<>();

        // If the incoming list contains "ALL" (case-insensitive), produce events for all tools.
        boolean containsAll = tools.stream().anyMatch(item -> "ALL".equalsIgnoreCase(item));
        if (containsAll) {
            finalTools.addAll(Arrays.asList(
                ToolTypes.CODE_SCAN,
                ToolTypes.DEPENDABOT,
                ToolTypes.SECRET_SCAN
            ));
        } else {
            // Otherwise, convert each string into a ToolTypes enum (if valid)
            for (String item : tools) {
                try {
                    // Assuming the incoming string is in uppercase or can be normalized
                    ToolTypes t = ToolTypes.valueOf(item.toUpperCase());
                    finalTools.add(t);
                } catch (IllegalArgumentException e) {
                    System.out.println("Skipping unknown tool: " + item);
                }
            }
        }

        // Publish an event for each tool in the final list
        for (ToolTypes tool : finalTools) {
            producerService.publishScanEvent(tool, tenantId);
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "Scan event(s) published successfully!");
        return response;
    }
}
