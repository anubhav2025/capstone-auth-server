package com.capstone.authServer.dto;

import com.capstone.authServer.enums.ToolTypes;

public class AlertUpdateDTO {

    private String tenantId;   // Instead of owner+repo
    private String esFindingId;
    private String alertNumber;
    private String newState;
    private String reason;
    private ToolTypes toolType;   // "CODE_SCAN","DEPENDABOT","SECRET_SCAN"

    // getters & setters
    public String getTenantId() {
        return tenantId;
    }
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getAlertNumber() {
        return alertNumber;
    }
    public void setAlertNumber(String alertNumber) {
        this.alertNumber = alertNumber;
    }

    public String getNewState() {
        return newState;
    }
    public void setNewState(String newState) {
        this.newState = newState;
    }

    public String getReason() {
        return reason;
    }
    public void setReason(String reason) {
        this.reason = reason;
    }

    public ToolTypes getToolType() {
        return toolType;
    }
    public void setToolType(ToolTypes toolType) {
        this.toolType = toolType;
    }
    public String getEsFindingId() {
        return esFindingId;
    }
    public void setEsFindingId(String esFindingId) {
        this.esFindingId = esFindingId;
    }
}
