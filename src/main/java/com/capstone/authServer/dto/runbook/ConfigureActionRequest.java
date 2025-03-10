package com.capstone.authServer.dto.runbook;

import com.capstone.authServer.model.FindingState;

public class ConfigureActionRequest {
    private Long runbookId;
    private ActionsDTO actions;

    public static class ActionsDTO {
        // if updateFinding is chosen
        private FindingState to;

        // if createTicket is chosen
        private Boolean createTicket;

        public FindingState getTo() {
            return to;
        }

        public void setTo(FindingState to) {
            this.to = to;
        }

        public Boolean getCreateTicket() {
            return createTicket;
        }

        public void setCreateTicket(Boolean createTicket) {
            this.createTicket = createTicket;
        }
    }

    // Getters / Setters
    public Long getRunbookId() {
        return runbookId;
    }

    public void setRunbookId(Long runbookId) {
        this.runbookId = runbookId;
    }

    public ActionsDTO getActions() {
        return actions;
    }

    public void setActions(ActionsDTO actions) {
        this.actions = actions;
    }
}

