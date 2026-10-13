package com.hotel.hotelreservation.service;

/** Business logic for the hotel reservation workflow. */

import com.hotel.hotelreservation.dto.UpgradeRequestBody;
import com.hotel.hotelreservation.model.*;
import com.hotel.hotelreservation.repository.ReservationRepository;
import com.hotel.hotelreservation.repository.RoomRepository;
import com.hotel.hotelreservation.repository.UpgradeRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UpgradeService {
    private final UpgradeRequestRepository upgradeRepository;
    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;

    // Implements upgradeService for this class.
    public UpgradeService(UpgradeRequestRepository upgradeRepository, ReservationRepository reservationRepository,
                          RoomRepository roomRepository) {
        this.upgradeRepository = upgradeRepository;
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
    }

    @Transactional
    // Creates or initiates request for the hotel workflow.
    public UpgradeRequest request(String email, UpgradeRequestBody body) {
        Reservation reservation = reservationRepository.findById(body.reservationId())
            .orElseThrow(() -> new IllegalArgumentException("Reservation not found."));
        if (!reservation.getCustomer().getEmail().equalsIgnoreCase(email)) {
            throw new org.springframework.security.access.AccessDeniedException("You cannot upgrade this reservation.");
        }
        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new IllegalArgumentException("Only confirmed reservations can be upgraded.");
        }
        Room requestedRoom = roomRepository.findById(body.requestedRoomId())
            .orElseThrow(() -> new IllegalArgumentException("Requested room not found."));
        if (requestedRoom.getId().equals(reservation.getRoom().getId())) {
            throw new IllegalArgumentException("Choose a different room.");
        }
        if (!requestedRoom.isAvailable()) throw new IllegalArgumentException("Requested room is not available.");
        UpgradeRequest upgrade = new UpgradeRequest();
        upgrade.setReservation(reservation);
        upgrade.setRequestedRoom(requestedRoom);
        return upgradeRepository.save(upgrade);
    }

    // Implements myRequests for this class.
    public List<UpgradeRequest> myRequests(String email) {
        return upgradeRepository.findByReservationCustomerEmailIgnoreCaseOrderByCreatedAtDesc(email);
    }

    // Retrieves pending data for this API operation.
    public List<UpgradeRequest> pending() {
        return upgradeRepository.findByStatusOrderByCreatedAtAsc(UpgradeStatus.PENDING);
    }

    @Transactional
    // Processes decide after checking the current workflow state.
    public UpgradeRequest decide(Long id, boolean approve) {
        UpgradeRequest request = upgradeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Upgrade request not found."));
        if (request.getStatus() != UpgradeStatus.PENDING) {
            throw new IllegalArgumentException("This request has already been processed.");
        }
        if (approve) {
            Reservation reservation = request.getReservation();
            Room newRoom = roomRepository.findById(request.getRequestedRoom().getId())
                .orElseThrow(() -> new IllegalArgumentException("Requested room not found."));
            if (!newRoom.isAvailable() || reservationRepository.hasOverlappingReservation(
                    newRoom.getId(), reservation.getCheckIn(), reservation.getCheckOut(), ReservationStatus.CONFIRMED)) {
                throw new IllegalArgumentException("Requested room is no longer available.");
            }
            reservation.setRoom(newRoom);
            reservationRepository.save(reservation);
            request.setStatus(UpgradeStatus.APPROVED);
        } else {
            request.setStatus(UpgradeStatus.REJECTED);
        }
        return upgradeRepository.save(request);
    }
}
