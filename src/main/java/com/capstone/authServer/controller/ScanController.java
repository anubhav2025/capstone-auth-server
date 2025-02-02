package com.capstone.authServer.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.capstone.authServer.dto.ScanEventDTO;
import com.capstone.authServer.service.ScanEventProducerService;

@RestController
@RequestMapping("/scan")
public class ScanController {

    private final ScanEventProducerService producerService;

    @Autowired
    public ScanController(ScanEventProducerService producerService) {
        this.producerService = producerService;
    }

    @PostMapping("/publish")
    public String publishScan(@RequestBody ScanEventDTO request) {
        producerService.publishScanEvent(request);
        return "Scan event published successfully!";
    }
}