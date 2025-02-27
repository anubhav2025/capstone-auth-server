package com.capstone.authServer.dto.ticketing;

public class UpdateTicketStatusRequestDTO {
    private String tenantId;
    private String ticketId;

    public UpdateTicketStatusRequestDTO() {
    }

    public UpdateTicketStatusRequestDTO(String tenantId, String ticketId) {
        this.tenantId = tenantId;
        this.ticketId = ticketId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }
}
