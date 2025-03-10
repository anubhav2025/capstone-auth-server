package com.capstone.authServer.dto.runbook;

import com.capstone.authServer.model.FindingSeverity;
import com.capstone.authServer.model.FindingState;

public class ConfigureFilterRequest {
    private Long runbookId;

    private FilterDTO filter;

    public static class FilterDTO {
        private FindingState state;    // can be null
        private FindingSeverity severity; // can be null

        public FindingState getState() {
            return state;
        }

        public void setState(FindingState state) {
            this.state = state;
        }

        public FindingSeverity getSeverity() {
            return severity;
        }

        public void setSeverity(FindingSeverity severity) {
            this.severity = severity;
        }
    }

    // Getters / Setters
    public Long getRunbookId() {
        return runbookId;
    }

    public void setRunbookId(Long runbookId) {
        this.runbookId = runbookId;
    }

    public FilterDTO getFilter() {
        return filter;
    }

    public void setFilter(FilterDTO filter) {
        this.filter = filter;
    }
}
