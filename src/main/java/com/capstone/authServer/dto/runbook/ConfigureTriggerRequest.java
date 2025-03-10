package com.capstone.authServer.dto.runbook;


import java.util.List;

import com.capstone.authServer.enums.TriggerType;

public class ConfigureTriggerRequest {
    private Long runbookId;
    private List<TriggerType> triggers;

    public Long getRunbookId() {
        return runbookId;
    }

    public void setRunbookId(Long runbookId) {
        this.runbookId = runbookId;
    }

    public List<TriggerType> getTriggers() {
        return triggers;
    }

    public void setTriggers(List<TriggerType> triggers) {
        this.triggers = triggers;
    }
}

