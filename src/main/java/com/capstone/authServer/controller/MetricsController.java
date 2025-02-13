package com.capstone.authServer.controller;

import com.capstone.authServer.dto.ScanToolType;
import com.capstone.authServer.security.RoleGuard;
import com.capstone.authServer.service.MetricsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class MetricsController {

    private final MetricsService metricsService;

    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    /**
     * Example: distribution of toolType across all findings for a tenant.
     */
    @GetMapping("/metrics/tool-distribution")
    @RoleGuard(allowed={"SUPER_ADMIN","ADMIN","USER"})
    public List<Map<String, Object>> getToolDistribution(@RequestParam String tenantId) throws Exception {
        // The service looks up Tenant.esIndex by tenantId and queries accordingly
        return metricsService.getToolDistribution(tenantId);
    }

    @GetMapping("/metrics/state-distribution")
    @RoleGuard(allowed={"SUPER_ADMIN","ADMIN","USER"})
    public List<Map<String, Object>> getStateDistribution(@RequestParam String tenantId,
                                                          @RequestParam ScanToolType toolType) throws Exception {
        return metricsService.getStateDistribution(tenantId, toolType);
    }

    @GetMapping("/metrics/severity-distribution")
    @RoleGuard(allowed={"SUPER_ADMIN","ADMIN","USER"})
    public List<Map<String, Object>> getSeverityDistribution(@RequestParam String tenantId,
                                                             @RequestParam ScanToolType toolType) throws Exception {
        return metricsService.getSeverityDistribution(tenantId, toolType);
    }

    @GetMapping("/metrics/cvss-histogram")
    @RoleGuard(allowed={"SUPER_ADMIN","ADMIN","USER"})
    public List<Map<String, Object>> getCvssHistogram(@RequestParam String tenantId) throws Exception {
        return metricsService.getCvssHistogram(tenantId);
    }
}
