package com.capstone.authServer.controller;

import com.capstone.authServer.dto.ticketing.CreateTicketRequestDTO;
import com.capstone.authServer.dto.ticketing.TicketResponseDTO;
import com.capstone.authServer.security.RoleGuard;
import com.capstone.authServer.service.CreateTicketEventProducerService;
import com.capstone.authServer.service.JiraTicketService;
import com.capstone.authServer.service.UpdateTicketEventProducerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    private final JiraTicketService jiraTicketService;
    private final UpdateTicketEventProducerService updateTicketEventProducerService;
    private final CreateTicketEventProducerService createTicketEventProducerService;

    public TicketController(JiraTicketService jiraTicketService,
                            UpdateTicketEventProducerService updateTicketEventProducerService,
                            CreateTicketEventProducerService createTicketEventProducerService) {
        this.jiraTicketService = jiraTicketService;
        this.updateTicketEventProducerService = updateTicketEventProducerService;
        this.createTicketEventProducerService = createTicketEventProducerService;
    }

    /**
     * Create a JIRA ticket for a given finding.
     */
    @PostMapping("/create")
    @RoleGuard(allowed={"SUPER_ADMIN","ADMIN","USER"})
    public ResponseEntity<?> createTicket(@RequestBody CreateTicketRequestDTO request) {
        try {
            // If your code uses an event-based approach:
            createTicketEventProducerService.produce(
                request.getTenantId(),
                request.getFindingId(),
                request.getSummary(),
                request.getDescription()
            );

            Map<String, String> response = new HashMap<>();
            response.put("message", "JIRA ticket creation initiated successfully.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Retrieve all tickets associated with a tenant.
     */
    @GetMapping
    @RoleGuard(allowed={"SUPER_ADMIN","ADMIN","USER"})
    public ResponseEntity<?> getAllTickets(@RequestParam("tenantId") String tenantId) {
        try {
            System.out.println("hello");
            List<TicketResponseDTO> tickets = jiraTicketService.getAllTicketsForTenant(tenantId);
            return ResponseEntity.ok(tickets);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Get a single ticket by ID.
     */
    @GetMapping("/{ticketId}")
    @RoleGuard(allowed={"SUPER_ADMIN","ADMIN","USER"})
    public ResponseEntity<?> getTicketById(
        @PathVariable("ticketId") String ticketId,
        @RequestParam("tenantId") String tenantId
    ) {
        try {
            TicketResponseDTO dto = jiraTicketService.getTicketById(tenantId, ticketId);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Transition a ticket from "To Do" to "Done".
     */
    @PutMapping("/{ticketId}/done")
    @RoleGuard(allowed={"SUPER_ADMIN","ADMIN","USER"})
    public ResponseEntity<?> updateTicketToDone(
            @PathVariable("ticketId") String ticketId,
            @RequestParam("tenantId") String tenantId
    ) {
        try {
            // If event-based approach:
            updateTicketEventProducerService.produce(ticketId, tenantId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Ticket status updated to Done (event).");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
