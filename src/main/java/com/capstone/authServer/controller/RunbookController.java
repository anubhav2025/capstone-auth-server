package com.capstone.authServer.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.capstone.authServer.dto.ApiResponse;
import com.capstone.authServer.dto.runbook.ConfigureActionRequest;
import com.capstone.authServer.dto.runbook.ConfigureFilterRequest;
import com.capstone.authServer.dto.runbook.ConfigureTriggerRequest;
import com.capstone.authServer.dto.runbook.CreateRunbookRequest;
import com.capstone.authServer.dto.runbook.RunbookStatusResponse;
import com.capstone.authServer.enums.TriggerType;
import com.capstone.authServer.model.runbook.Runbook;
import com.capstone.authServer.model.runbook.RunbookAction;
import com.capstone.authServer.model.runbook.RunbookFilter;
import com.capstone.authServer.model.runbook.RunbookTrigger;
import com.capstone.authServer.service.RunbookService;

import java.util.List;

@RestController
@RequestMapping("/runbooks")
public class RunbookController {

    private final RunbookService runbookService;

    public RunbookController(RunbookService runbookService) {
        this.runbookService = runbookService;
    }

    @GetMapping
    public ResponseEntity<?> getRunbooksByTenant(
            @RequestParam("tenantId") String tenantId
    ) {
        try {
            List<Runbook> runbooks = runbookService.getRunbooksByTenant(tenantId);
            return ResponseEntity.ok(runbooks);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createRunbook(@RequestBody CreateRunbookRequest request) {
        try {
            Runbook runbook = runbookService.createRunbook(request);
            return ResponseEntity.ok(runbook);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/status/{runbookId}")
    public ResponseEntity<RunbookStatusResponse> checkRunbookStatus(@PathVariable Long runbookId) {
        List<String> statusList = runbookService.checkRunbookStatus(runbookId);
        return ResponseEntity.ok(new RunbookStatusResponse(statusList));
    }

    // Configure triggers
    @PostMapping("/configure-triggers")
    public ResponseEntity<?> configureTriggers(@RequestBody ConfigureTriggerRequest request) {
        try {
            runbookService.configureTriggers(request);
            return ResponseEntity.ok(new ApiResponse("Triggers configured successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Returns the list of possible triggers (currently just [NEW_SCAN_INITIATE])
    @GetMapping("/available-triggers")
    public ResponseEntity<List<TriggerType>> getAllAvailableTriggers() {
        return ResponseEntity.ok(runbookService.getAllAvailableTriggers());
    }

    // Retrieve triggers for a runbook
    @GetMapping("/{runbookId}/triggers")
    public ResponseEntity<?> getTriggersForRunbook(@PathVariable("runbookId") Long runbookId) {
        try {
            List<RunbookTrigger> triggers = runbookService.getTriggersForRunbook(runbookId);
            return ResponseEntity.ok(triggers);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Configure filter
    @PostMapping("/configure-filters")
    public ResponseEntity<?> configureFilters(@RequestBody ConfigureFilterRequest request) {
        try {
            runbookService.configureFilter(request);
            return ResponseEntity.ok(new ApiResponse("Filter configured successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{runbookId}/filters")
    public ResponseEntity<?> getFiltersForRunbook(@PathVariable("runbookId") Long runbookId) {
        try {
            List<RunbookFilter> filters = runbookService.getFiltersForRunbook(runbookId);
            return ResponseEntity.ok(filters);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Configure actions
    @PostMapping("/configure-actions")
    public ResponseEntity<?> configureActions(@RequestBody ConfigureActionRequest request) {
        try {
            runbookService.configureActions(request);
            return ResponseEntity.ok(new ApiResponse("Actions configured successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{runbookId}/actions")
    public ResponseEntity<?> getActionsForRunbook(@PathVariable("runbookId") Long runbookId) {
        try {
            List<RunbookAction> actions = runbookService.getActionsForRunbook(runbookId);
            return ResponseEntity.ok(actions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Enable/Disable runbook
    @PutMapping("/{runbookId}/enable")
    public ResponseEntity<?> setRunbookEnabled(@PathVariable("runbookId") Long runbookId,
                                               @RequestParam("enable") boolean enable) {
        try {
            runbookService.setRunbookEnabled(runbookId, enable);
            return ResponseEntity.ok(new ApiResponse("Runbook enable status updated to: " + enable));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{runbookId}")
    public ResponseEntity<?> deleteRunbook(@PathVariable Long runbookId) {
        try {
            runbookService.deleteRunbook(runbookId);
            return ResponseEntity.ok(new ApiResponse("Runbook deleted successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
