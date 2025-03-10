package com.capstone.authServer.service;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capstone.authServer.dto.runbook.ConfigureActionRequest;
import com.capstone.authServer.dto.runbook.ConfigureFilterRequest;
import com.capstone.authServer.dto.runbook.ConfigureTriggerRequest;
import com.capstone.authServer.dto.runbook.CreateRunbookRequest;
import com.capstone.authServer.enums.ActionType;
import com.capstone.authServer.enums.TriggerType;
import com.capstone.authServer.model.FindingSeverity;
import com.capstone.authServer.model.FindingState;
import com.capstone.authServer.model.runbook.Runbook;
import com.capstone.authServer.model.runbook.RunbookAction;
import com.capstone.authServer.model.runbook.RunbookFilter;
import com.capstone.authServer.model.runbook.RunbookTrigger;
import com.capstone.authServer.repository.RunbookActionRepository;
import com.capstone.authServer.repository.RunbookFilterRepository;
import com.capstone.authServer.repository.RunbookRepository;
import com.capstone.authServer.repository.RunbookTriggerRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RunbookService {

    private final RunbookRepository runbookRepository;
    private final RunbookTriggerRepository triggerRepository;
    private final RunbookFilterRepository filterRepository;
    private final RunbookActionRepository actionRepository;

    public RunbookService(RunbookRepository runbookRepository,
                          RunbookTriggerRepository triggerRepository,
                          RunbookFilterRepository filterRepository,
                          RunbookActionRepository actionRepository) {
        this.runbookRepository = runbookRepository;
        this.triggerRepository = triggerRepository;
        this.filterRepository = filterRepository;
        this.actionRepository = actionRepository;
    }

    // Create a new runbook
    public Runbook createRunbook(CreateRunbookRequest request) {
        Runbook runbook = new Runbook();
        runbook.setTenantId(request.getTenantId());
        runbook.setName(request.getName());
        runbook.setDescription(request.getDescription());
        runbook.setEnabled(true);

        return runbookRepository.save(runbook);
    }

    // Check which components are configured (Trigger, Filter, Action)
    public List<String> checkRunbookStatus(Long runbookId) {
        List<String> status = new ArrayList<>();
        Optional<Runbook> optional = runbookRepository.findById(runbookId);
        if (!optional.isPresent()) {
            return status; 
        }
        Runbook runbook = optional.get();

        if (!triggerRepository.findByRunbook(runbook).isEmpty()) {
            status.add("TRIGGER");
        }
        if (!filterRepository.findByRunbook(runbook).isEmpty()) {
            status.add("FILTER");
        }
        if (!actionRepository.findByRunbook(runbook).isEmpty()) {
            status.add("ACTION");
        }

        return status;
    }

    // Configure triggers (accepts a list, even if it currently has only NEW_SCAN_INITIATE)
    @Transactional
    public void configureTriggers(ConfigureTriggerRequest request) {
        Runbook runbook = runbookRepository.findById(request.getRunbookId())
                .orElseThrow(() -> new RuntimeException("Runbook not found"));

        // Clear old triggers
        List<RunbookTrigger> oldTriggers = triggerRepository.findByRunbook(runbook);
        triggerRepository.deleteAll(oldTriggers);

        // Create new triggers
        for (TriggerType t : request.getTriggers()) {
            RunbookTrigger rt = new RunbookTrigger(runbook, t);
            triggerRepository.save(rt);
        }
    }

    public List<RunbookTrigger> getTriggersForRunbook(Long runbookId) {
        Runbook runbook = runbookRepository.findById(runbookId)
                .orElseThrow(() -> new RuntimeException("Runbook not found"));
        return triggerRepository.findByRunbook(runbook);
    }

    // Returns all "possible" triggers from the enum
    public List<TriggerType> getAllAvailableTriggers() {
        return List.of(TriggerType.values());
    }

    // Configure filter (replace with one row)
    @Transactional
    public void configureFilter(ConfigureFilterRequest request) {
        Runbook runbook = runbookRepository.findById(request.getRunbookId())
                .orElseThrow(() -> new RuntimeException("Runbook not found"));

        filterRepository.deleteAll(filterRepository.findByRunbook(runbook));

        if (request.getFilter() != null) {
            FindingState state = request.getFilter().getState();
            FindingSeverity severity = request.getFilter().getSeverity();

            RunbookFilter filter = new RunbookFilter(runbook, state, severity);
            filterRepository.save(filter);
        }
    }

    public List<RunbookFilter> getFiltersForRunbook(Long runbookId) {
        Runbook runbook = runbookRepository.findById(runbookId)
                .orElseThrow(() -> new RuntimeException("Runbook not found"));
        return filterRepository.findByRunbook(runbook);
    }

    // Configure actions (can store multiple if needed)
    @Transactional
    public void configureActions(ConfigureActionRequest request) {
        Runbook runbook = runbookRepository.findById(request.getRunbookId())
                .orElseThrow(() -> new RuntimeException("Runbook not found"));

        // Clear existing actions
        actionRepository.deleteAll(actionRepository.findByRunbook(runbook));

        ConfigureActionRequest.ActionsDTO dto = request.getActions();
        if (dto == null) return;

        // If user selected "Update Finding"
        if (dto.getTo() != null) {
            RunbookAction updateAction = new RunbookAction(
                    runbook,
                    // For now we represent update-finding with ActionType.UPDATE_FINDING
                    ActionType.UPDATE_FINDING,
                    dto.getTo(),
                    null
            );
            actionRepository.save(updateAction);
        }

        // If user selected "Create Ticket"
        if (Boolean.TRUE.equals(dto.getCreateTicket())) {
            RunbookAction createTicketAction = new RunbookAction(
                    runbook,
                    ActionType.CREATE_TICKET,
                    null,
                    true
            );
            actionRepository.save(createTicketAction);
        }
    }

    // RunbookService.java (excerpt)

    public List<Runbook> getRunbooksByTenant(String tenantId) {
        return runbookRepository.findByTenantId(tenantId);
    }


    public List<RunbookAction> getActionsForRunbook(Long runbookId) {
        Runbook runbook = runbookRepository.findById(runbookId)
                .orElseThrow(() -> new RuntimeException("Runbook not found"));

        return actionRepository.findByRunbook(runbook);
    }

    // Enable/disable a runbook
    @Transactional
    public void setRunbookEnabled(Long runbookId, boolean enable) {
        Runbook runbook = runbookRepository.findById(runbookId)
                .orElseThrow(() -> new RuntimeException("Runbook not found"));
        runbook.setEnabled(enable);
        runbookRepository.save(runbook);
    }

    public void deleteRunbook(Long runbookId) {
        Runbook runbook = runbookRepository.findById(runbookId)
                .orElseThrow(() -> new RuntimeException("Runbook not found"));

        // Manually delete child records, unless you have a cascading relationship
        triggerRepository.deleteAll(triggerRepository.findByRunbook(runbook));
        filterRepository.deleteAll(filterRepository.findByRunbook(runbook));
        actionRepository.deleteAll(actionRepository.findByRunbook(runbook));

        // Finally, delete the runbook itself
        runbookRepository.delete(runbook);
    }
}
