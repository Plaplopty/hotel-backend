package com.hotel.hotelreservation.controller;

/** REST API endpoints for the related hotel reservation feature. */

import com.hotel.hotelreservation.dto.UpgradeRequestBody;
import com.hotel.hotelreservation.model.UpgradeRequest;
import com.hotel.hotelreservation.service.UpgradeService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/upgrades")
public class UpgradeController {
    private final UpgradeService upgradeService;

    // Implements upgradeController for this class.
    public UpgradeController(UpgradeService upgradeService) { this.upgradeService = upgradeService; }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    // Creates or initiates request for the hotel workflow.
    public UpgradeRequest request(Authentication authentication, @Valid @RequestBody UpgradeRequestBody body) {
        return upgradeService.request(authentication.getName(), body);
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('CUSTOMER')")
    // Retrieves mine data for this API operation.
    public List<UpgradeRequest> mine(Authentication authentication) {
        return upgradeService.myRequests(authentication.getName());
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('MANAGER')")
    // Retrieves pending data for this API operation.
    public List<UpgradeRequest> pending() { return upgradeService.pending(); }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('MANAGER')")
    // Processes approve after checking the current workflow state.
    public UpgradeRequest approve(@PathVariable Long id) { return upgradeService.decide(id, true); }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('MANAGER')")
    // Processes reject while preserving authorization and state rules.
    public UpgradeRequest reject(@PathVariable Long id) { return upgradeService.decide(id, false); }

    @GetMapping("/health")
    // Implements health for this class.
    public Map<String, String> health() { return Map.of("status", "ok"); }
}
