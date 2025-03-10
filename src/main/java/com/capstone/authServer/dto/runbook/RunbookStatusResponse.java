package com.capstone.authServer.dto.runbook;

import java.util.List;

public class RunbookStatusResponse {
    private List<String> status;  // e.g. ["TRIGGER","FILTER"]

    public RunbookStatusResponse(List<String> status) {
        this.status = status;
    }

    public List<String> getStatus() {
        return status;
    }

    public void setStatus(List<String> status) {
        this.status = status;
    }
}

