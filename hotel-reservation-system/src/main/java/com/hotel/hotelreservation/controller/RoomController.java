package com.hotel.hotelreservation.controller;

/** REST API endpoints for the related hotel reservation feature. */

import com.hotel.hotelreservation.model.Room;
import com.hotel.hotelreservation.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {
    private final RoomService roomService;

    // Implements roomController for this class.
    public RoomController(RoomService roomService) { this.roomService = roomService; }

    @GetMapping
    // Retrieves all data for this API operation.
    public List<Room> all() { return roomService.findAll(); }

    @GetMapping("/{id}")
    // Retrieves one data for this API operation.
    public Room one(@PathVariable Long id) { return roomService.findById(id); }

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    // Creates or initiates create for the hotel workflow.
    public Room create(@Valid @RequestBody Room room) { return roomService.create(room); }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    // Updates update after business-rule validation.
    public Room update(@PathVariable Long id, @Valid @RequestBody Room room) {
        return roomService.update(id, room);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    // Processes delete while preserving authorization and state rules.
    public void delete(@PathVariable Long id) { roomService.delete(id); }

    /** Finds rooms marked available and not already booked across the requested date range. */
    @GetMapping("/availability")
    // Retrieves availability data for this API operation.
    public List<Room> availability(@RequestParam java.time.LocalDate checkIn,
                                   @RequestParam java.time.LocalDate checkOut) {
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)
                || checkIn.isBefore(java.time.LocalDate.now())) {
            throw new IllegalArgumentException("Provide a future check-in and a later check-out date.");
        }
        return roomService.availableFor(checkIn, checkOut);
    }

    /** Manager changes the room's manual availability flag. */
    @PatchMapping("/{id}/availability")
    @PreAuthorize("hasRole('MANAGER')")
    // Updates updateAvailability after business-rule validation.
    public Room updateAvailability(@PathVariable Long id, @RequestParam boolean available) {
        return roomService.setAvailability(id, available);
    }
}
