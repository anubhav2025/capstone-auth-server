package com.capstone.authServer.utils;

import com.capstone.authServer.dto.findings.FindingResponseDTO;
import com.capstone.authServer.model.Finding;

public class FindingToFindingResponseDTO {
    public static FindingResponseDTO convert(Finding finding) {
        FindingResponseDTO dto = new FindingResponseDTO();
        dto.setId(finding.getId());
        dto.setTitle(finding.getTitle());
        dto.setDesc(finding.getDesc());
        dto.setSeverity(finding.getSeverity());
        dto.setState(finding.getState());
        dto.setUrl(finding.getUrl());
        dto.setToolType(finding.getToolType());
        dto.setCve(finding.getCve());
        dto.setCwes(finding.getCwes());
        dto.setCvss(finding.getCvss());
        dto.setType(finding.getType());
        dto.setSuggestions(finding.getSuggestions());
        dto.setFilePath(finding.getFilePath());
        dto.setComponentName(finding.getComponentName());
        dto.setComponentVersion(finding.getComponentVersion());
        dto.setCreatedAt(finding.getCreatedAt());
        dto.setUpdatedAt(finding.getUpdatedAt());
        dto.setTicketId(finding.getTicketId());

        return dto;
    }
}
