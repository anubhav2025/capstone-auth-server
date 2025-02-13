package com.capstone.authServer.dto;

import java.util.List;

public class ScanEventDTO {

    private String tenantId;
    private List<ScanType> tools;

    public ScanEventDTO() {
    }

    public ScanEventDTO(String tenantId, List<ScanType> tools) {
        this.tenantId = tenantId;
        this.tools = tools;
    }

    public String getTenantId() {
        return tenantId;
    }
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public List<ScanType> getTools() {
        return tools;
    }
    public void setTools(List<ScanType> tools) {
        this.tools = tools;
    }
}
