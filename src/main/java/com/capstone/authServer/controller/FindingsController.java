package com.capstone.authServer.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.capstone.authServer.dto.findings.FindingResponseDTO;
import com.capstone.authServer.enums.ToolTypes;
import com.capstone.authServer.model.Finding;
import com.capstone.authServer.model.FindingSeverity;
import com.capstone.authServer.model.FindingState;
import com.capstone.authServer.model.SearchFindingsResult;
import com.capstone.authServer.security.RoleGuard;
import com.capstone.authServer.service.ElasticSearchService;
import com.capstone.authServer.utils.FindingToFindingResponseDTO;

@RestController
public class FindingsController {

    private final ElasticSearchService service;

    public FindingsController(ElasticSearchService service) {
        this.service = service;
    }

    @GetMapping("/findings")
    @RoleGuard(allowed={"SUPER_ADMIN","ADMIN","USER"})
    public Map<String, Object> getFindings(
            @RequestParam(required = false) String tenantId,          // The tenant ID
            @RequestParam(required = false) ToolTypes toolType,
            @RequestParam(required = false) FindingSeverity severity,
            @RequestParam(required = false) FindingState state,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        // If tenantId is null or empty, you might fallback to the user's default tenant 
        // or throw an error. This is your design choice. 
        // Below we assume tenantId must be provided, or you'd do a fallback.

        SearchFindingsResult searchResult = service.searchFindings(
                tenantId,  // pass tenantId to the service 
                toolType,
                severity,
                state,
                page,
                size
        );

        List<FindingResponseDTO> dtoList = searchResult.getFindings().stream()
                .map(FindingToFindingResponseDTO::convert)
                .collect(Collectors.toList());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "success");
        response.put("findingsCount", dtoList.size());
        response.put("findingsTotal", searchResult.getTotal());
        response.put("findings", dtoList);

        return response;
    }
}
