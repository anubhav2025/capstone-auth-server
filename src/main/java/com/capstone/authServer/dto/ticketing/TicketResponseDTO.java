package com.capstone.authServer.dto.ticketing;

public class TicketResponseDTO {
    private String ticketId;            
    private String issueTypeName;       
    private String issueTypeDescription;
    private String summary;            
    private String statusName;

    // NEW:
    private String findingId; // e.g. the ES finding ID

    public TicketResponseDTO() {
    }

    public TicketResponseDTO(String ticketId,
                             String issueTypeName,
                             String issueTypeDescription,
                             String summary,
                             String statusName) {
        this.ticketId = ticketId;
        this.issueTypeName = issueTypeName;
        this.issueTypeDescription = issueTypeDescription;
        this.summary = summary;
        this.statusName = statusName;
    }

    // Getters & Setters
    public String getTicketId() {
        return ticketId;
    }
    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getIssueTypeName() {
        return issueTypeName;
    }
    public void setIssueTypeName(String issueTypeName) {
        this.issueTypeName = issueTypeName;
    }

    public String getIssueTypeDescription() {
        return issueTypeDescription;
    }
    public void setIssueTypeDescription(String issueTypeDescription) {
        this.issueTypeDescription = issueTypeDescription;
    }

    public String getSummary() {
        return summary;
    }
    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getStatusName() {
        return statusName;
    }
    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public String getFindingId() {
        return findingId;
    }
    public void setFindingId(String findingId) {
        this.findingId = findingId;
    }
}
