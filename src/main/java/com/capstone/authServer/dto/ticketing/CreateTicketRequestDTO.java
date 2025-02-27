package com.capstone.authServer.dto.ticketing;

public class CreateTicketRequestDTO {

    private String tenantId;
    private String findingId;
    private String summary;
    private String description;

    // Constructors
    public CreateTicketRequestDTO() {
    }

    public CreateTicketRequestDTO(String tenantId, String findingId, String summary, String description) {
        this.tenantId = tenantId;
        this.findingId = findingId;
        this.summary = summary;
        this.description = description;
    }

    // Getters and Setters
    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getFindingId() {
        return findingId;
    }

    public void setFindingId(String findingId) {
        this.findingId = findingId;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
