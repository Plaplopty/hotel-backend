package com.hotel.hotelreservation.service;

/** Business logic for the hotel reservation workflow. */

import com.hotel.hotelreservation.dto.ReservationRequest;
import com.hotel.hotelreservation.model.*;
import com.hotel.hotelreservation.repository.ReservationRepository;
import com.hotel.hotelreservation.repository.RoomRepository;
import com.hotel.hotelreservation.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    // Implements reservationService for this class.
    public ReservationService(ReservationRepository reservationRepository, RoomRepository roomRepository,
                              UserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    // Creates or initiates create for the hotel workflow.
    public Reservation create(String authenticatedEmail, ReservationRequest request) {
        if (!request.checkIn().isAfter(LocalDate.now().minusDays(1))) {
            throw new IllegalArgumentException("Check-in must be today or a future date.");
        }
        if (!request.checkOut().isAfter(request.checkIn())) {
            throw new IllegalArgumentException("Check-out must be after check-in.");
        }
        Room room = roomRepository.findById(request.roomId())
            .orElseThrow(() -> new IllegalArgumentException("Room not found."));
        if (!room.isAvailable()) throw new IllegalArgumentException("Room is not available.");
        if (reservationRepository.hasOverlappingReservation(room.getId(), request.checkIn(),
                request.checkOut(), ReservationStatus.CONFIRMED)) {
            throw new IllegalArgumentException("Room is already booked for those dates.");
        }
        User customer = userRepository.findByEmailIgnoreCase(authenticatedEmail)
            .orElseThrow(() -> new IllegalArgumentException("Customer account not found."));
        Reservation reservation = new Reservation();
        reservation.setCustomer(customer);
        reservation.setRoom(room);
        reservation.setCheckIn(request.checkIn());
        reservation.setCheckOut(request.checkOut());
        reservation.setStatus(ReservationStatus.CONFIRMED);
        return reservationRepository.save(reservation);
    }

    // Implements myReservations for this class.
    public List<Reservation> myReservations(String email) {
        return reservationRepository.findByCustomerEmailIgnoreCaseOrderByCreatedAtDesc(email);
    }

    // Retrieves allReservations data for this API operation.
    public List<Reservation> allReservations() {
        return reservationRepository.findAll();
    }

    @Transactional
    // Processes cancel while preserving authorization and state rules.
    public Reservation cancel(Long id, String email, boolean manager) {
        Reservation reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Reservation not found."));
        if (!manager && !reservation.getCustomer().getEmail().equalsIgnoreCase(email)) {
            throw new org.springframework.security.access.AccessDeniedException("You cannot cancel this reservation.");
        }
        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new IllegalArgumentException("Only confirmed reservations can be cancelled.");
        }
        // Customers submit a request; managers can cancel immediately after authorization.
        reservation.setStatus(manager ? ReservationStatus.CANCELLED : ReservationStatus.CANCELLATION_REQUESTED);
        return reservationRepository.save(reservation);
    }

    /** Manager approves or rejects a pending cancellation request. */
    @Transactional
    // Processes decideCancellation after checking the current workflow state.
    public Reservation decideCancellation(Long id, boolean approve) {
        Reservation reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Reservation not found."));
        if (reservation.getStatus() != ReservationStatus.CANCELLATION_REQUESTED)
            throw new IllegalArgumentException("No pending cancellation request exists.");
        reservation.setStatus(approve ? ReservationStatus.CANCELLED : ReservationStatus.CONFIRMED);
        return reservationRepository.save(reservation);
    }

    /** Manager updates the reservation lifecycle after validating the requested state. */
    @Transactional
    // Updates updateStatus after business-rule validation.
    public Reservation updateStatus(Long id, ReservationStatus status) {
        Reservation reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Reservation not found."));
        if (status == ReservationStatus.CANCELLATION_REQUESTED)
            throw new IllegalArgumentException("Use the cancellation request endpoint for this state.");
        reservation.setStatus(status);
        return reservationRepository.save(reservation);
    }
}
