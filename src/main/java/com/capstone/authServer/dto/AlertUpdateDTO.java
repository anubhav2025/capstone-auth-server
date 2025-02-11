package com.capstone.authServer.dto;


/**
 * The body we receive from the React app when user sets new state.
 */
public class AlertUpdateDTO {

    private String owner;
    private String repo;
    private String toolType;   // "CODE_SCAN","DEPENDABOT","SECRET_SCAN"
    private String alertNumber;
    private String newState;   // e.g. "dismissed","open","resolved"
    private String reason;     // e.g. "FIX_STARTED","FALSE_POSITIVE", etc.

    // getters & setters
    public String getOwner() {
        return owner;
    }
    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getRepo() {
        return repo;
    }
    public void setRepo(String repo) {
        this.repo = repo;
    }

    public String getToolType() {
        return toolType;
    }
    public void setToolType(String toolType) {
        this.toolType = toolType;
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
}

