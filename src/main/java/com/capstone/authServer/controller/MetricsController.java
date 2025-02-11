package com.capstone.authServer.controller;

import com.capstone.authServer.dto.ScanToolType;
import com.capstone.authServer.security.RoleGuard;
import com.capstone.authServer.service.MetricsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Endpoints to power the dashboard charts.
 */
@RestController
public class MetricsController {

    private final MetricsService metricsService;

    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    /**
     * Pie chart: overall distribution of toolType across all findings
     */
    @GetMapping("/metrics/tool-distribution")
    @RoleGuard(allowed={"SUPER_ADMIN","ADMIN", "USER"})
    public List<Map<String, Object>> getToolDistribution() throws Exception {
        // returns e.g. [ { "toolType": "CODE_SCAN", "count": 15 }, ... ]
        return metricsService.getToolDistribution();
    }

    /**
     * State distribution for a selected tool type
     */
    @GetMapping("/metrics/state-distribution")
    @RoleGuard(allowed={"SUPER_ADMIN","ADMIN", "USER"})
    public List<Map<String, Object>> getStateDistribution(@RequestParam ScanToolType toolType) throws Exception {
        // e.g. [ { "state": "OPEN", "count": 10 }, { "state": "DISMISSED", "count": 5 }, ... ]
        return metricsService.getStateDistribution(toolType);
    }

    /**
     * Severity distribution for a selected tool type
     */
    @GetMapping("/metrics/severity-distribution")
    @RoleGuard(allowed={"SUPER_ADMIN","ADMIN", "USER"})
    public List<Map<String, Object>> getSeverityDistribution(@RequestParam ScanToolType toolType) throws Exception {
        // e.g. [ { "severity": "CRITICAL", "count": 2 }, { "severity": "HIGH", "count": 5 }, ... ]
        return metricsService.getSeverityDistribution(toolType);
    }

    @GetMapping("/metrics/cvss-histogram")
    @RoleGuard(allowed={"SUPER_ADMIN","ADMIN", "USER"})
    public List<Map<String, Object>> getCvssHistogram() throws Exception {
        return metricsService.getCvssHistogram();
    }
}
