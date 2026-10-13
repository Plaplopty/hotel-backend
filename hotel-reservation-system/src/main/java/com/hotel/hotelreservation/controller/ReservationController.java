package com.hotel.hotelreservation.controller;

/** REST API endpoints for the related hotel reservation feature. */

import com.hotel.hotelreservation.dto.ReservationRequest;
import com.hotel.hotelreservation.model.Reservation;
import com.hotel.hotelreservation.model.ReservationStatus;
import com.hotel.hotelreservation.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
    private final ReservationService reservationService;

    // Implements reservationController for this class.
    public ReservationController(ReservationService reservationService) { this.reservationService = reservationService; }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    // Creates or initiates create for the hotel workflow.
    public Reservation create(Authentication authentication, @Valid @RequestBody ReservationRequest request) {
        return reservationService.create(authentication.getName(), request);
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('CUSTOMER')")
    // Retrieves mine data for this API operation.
    public List<Reservation> mine(Authentication authentication) {
        return reservationService.myReservations(authentication.getName());
    }

    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    // Retrieves all data for this API operation.
    public List<Reservation> all() { return reservationService.allReservations(); }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('CUSTOMER','MANAGER')")
    // Processes cancel while preserving authorization and state rules.
    public Reservation cancel(@PathVariable Long id, Authentication authentication) {
        boolean manager = authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_MANAGER"));
        return reservationService.cancel(id, authentication.getName(), manager);
    }

    /** Manager approves a customer's cancellation request. */
    @PatchMapping("/{id}/cancellation/approve")
    @PreAuthorize("hasRole('MANAGER')")
    // Processes approveCancellation after checking the current workflow state.
    public Reservation approveCancellation(@PathVariable Long id) {
        return reservationService.decideCancellation(id, true);
    }

    /** Manager rejects a customer's cancellation request and restores confirmation. */
    @PatchMapping("/{id}/cancellation/reject")
    @PreAuthorize("hasRole('MANAGER')")
    // Processes rejectCancellation while preserving authorization and state rules.
    public Reservation rejectCancellation(@PathVariable Long id) {
        return reservationService.decideCancellation(id, false);
    }

    /** Manager changes a reservation status; request-only state is deliberately disallowed. */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('MANAGER')")
    // Updates updateStatus after business-rule validation.
    public Reservation updateStatus(@PathVariable Long id, @RequestParam ReservationStatus status) {
        return reservationService.updateStatus(id, status);
    }

    /** Manager explicitly approves a reservation (idempotent for already-confirmed records). */
    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('MANAGER')")
    // Processes approve after checking the current workflow state.
    public Reservation approve(@PathVariable Long id) {
        return reservationService.updateStatus(id, ReservationStatus.CONFIRMED);
    }

    /** Manager rejects a reservation without deleting its audit trail. */
    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('MANAGER')")
    // Processes reject while preserving authorization and state rules.
    public Reservation reject(@PathVariable Long id) {
        return reservationService.updateStatus(id, ReservationStatus.REJECTED);
    }
}
