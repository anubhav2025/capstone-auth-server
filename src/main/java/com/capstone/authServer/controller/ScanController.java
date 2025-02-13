package com.capstone.authServer.controller;

import com.capstone.authServer.dto.ScanEventDTO;
import com.capstone.authServer.security.RoleGuard;
import com.capstone.authServer.service.ScanEventProducerService;

import java.util.HashMap;
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
    @RoleGuard(allowed={"SUPER_ADMIN","ADMIN"})
    public Map<String, String> publishScan(@RequestParam("tenantId") String tenantId, @RequestBody ScanEventDTO request) {
        System.out.println(request.getTenantId());
        System.out.println(request.getTools());
        // request.tenantId is included
        producerService.publishScanEvent(request);
         Map<String, String> response = new HashMap<>();
        response.put("message", "Scan event published successfully!");
        return response;
    }
}
