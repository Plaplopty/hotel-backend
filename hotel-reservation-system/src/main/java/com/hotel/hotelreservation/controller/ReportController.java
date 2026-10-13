package com.hotel.hotelreservation.controller;

/** REST API endpoints for the related hotel reservation feature. */

import com.hotel.hotelreservation.model.ReservationStatus;
import com.hotel.hotelreservation.repository.ReservationRepository;
import com.hotel.hotelreservation.repository.RoomRepository;
import com.hotel.hotelreservation.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasRole('MANAGER')")
public class ReportController {
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;

    // Implements reportController for this class.
    public ReportController(UserRepository userRepository, RoomRepository roomRepository,
                            ReservationRepository reservationRepository) {
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.reservationRepository = reservationRepository;
    }

    @GetMapping("/summary")
    // Retrieves summary data for this API operation.
    public Map<String, Long> summary() {
        long confirmed = reservationRepository.findAll().stream()
            .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED).count();
        long cancelled = reservationRepository.findAll().stream()
            .filter(r -> r.getStatus() == ReservationStatus.CANCELLED).count();
        return Map.of(
            "customers", userRepository.count(),
            "rooms", roomRepository.count(),
            "reservations", reservationRepository.count(),
            "confirmedReservations", confirmed,
            "cancelledReservations", cancelled
        );
    }

    /** Exports reservation data as UTF-8 CSV for spreadsheet/report use. */
    @GetMapping(value = "/reservations/export", produces = "text/csv")
    // Implements exportReservations for this class.
    public ResponseEntity<byte[]> exportReservations() {
        String csv = "id,customerEmail,roomNumber,checkIn,checkOut,status\n" +
            reservationRepository.findAll().stream().map(r -> String.join(",",
                csv(r.getId().toString()), csv(r.getCustomer().getEmail()), csv(r.getRoom().getRoomNumber()),
                csv(r.getCheckIn().toString()), csv(r.getCheckOut().toString()), csv(r.getStatus().name())))
                .collect(Collectors.joining("\n"));
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reservations.csv")
            .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
            .body(csv.getBytes(StandardCharsets.UTF_8));
    }

    /** Exports room inventory and nightly rates as UTF-8 CSV. */
    @GetMapping(value = "/rooms/export", produces = "text/csv")
    // Implements exportRooms for this class.
    public ResponseEntity<byte[]> exportRooms() {
        String csv = "id,roomNumber,roomType,pricePerNight,available,description\n" +
            roomRepository.findAll().stream().map(r -> String.join(",", csv(r.getId().toString()),
                csv(r.getRoomNumber()), csv(r.getRoomType()), csv(r.getPricePerNight().toPlainString()),
                csv(Boolean.toString(r.isAvailable())), csv(r.getDescription() == null ? "" : r.getDescription())))
                .collect(Collectors.joining("\n"));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=rooms.csv")
            .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8)).body(csv.getBytes(StandardCharsets.UTF_8));
    }

    /** CSV-escapes fields and neutralizes spreadsheet formula injection. */
    private String csv(String value) {
        String safe = value == null ? "" : value;
        if (!safe.isEmpty() && "=+-@\t\r".indexOf(safe.charAt(0)) >= 0) safe = "'" + safe;
        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }

    /** Reports estimated booked revenue from confirmed stays, using nightly room rates. */
    @GetMapping("/revenue")
    // Retrieves revenue data for this API operation.
    public Map<String, Object> revenue() {
        java.math.BigDecimal total = reservationRepository.findAll().stream()
            .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED)
            .map(r -> r.getRoom().getPricePerNight().multiply(java.math.BigDecimal.valueOf(
                java.time.temporal.ChronoUnit.DAYS.between(r.getCheckIn(), r.getCheckOut()))))
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        return Map.of("currencyNote", "Amounts use configured room rates; no currency conversion applied.",
            "estimatedBookedRevenue", total, "currency", "configured room-rate currency");
    }
}
