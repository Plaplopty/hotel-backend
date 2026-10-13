package com.hotel.hotelreservation.service;

/** Business logic for the hotel reservation workflow. */

import com.hotel.hotelreservation.model.Room;
import com.hotel.hotelreservation.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoomService {
    private final RoomRepository roomRepository;
    private final com.hotel.hotelreservation.repository.ReservationRepository reservationRepository;

    // Implements roomService for this class.
    public RoomService(RoomRepository roomRepository,
                       com.hotel.hotelreservation.repository.ReservationRepository reservationRepository) {
        this.roomRepository = roomRepository;
        this.reservationRepository = reservationRepository;
    }

    /** Lists all rooms for the public room browser. */
    public List<Room> findAll() { return roomRepository.findAll(); }

    /** Filters out rooms with confirmed overlapping reservations for the selected dates. */
    public List<Room> availableFor(java.time.LocalDate checkIn, java.time.LocalDate checkOut) {
        return roomRepository.findAll().stream().filter(Room::isAvailable)
            .filter(room -> !reservationRepository.hasOverlappingReservation(room.getId(), checkIn, checkOut,
                com.hotel.hotelreservation.model.ReservationStatus.CONFIRMED))
            .toList();
    }

    /** Updates the manager-controlled availability flag. */
    @Transactional
    // Updates setAvailability after business-rule validation.
    public Room setAvailability(Long id, boolean available) {
        Room room = findById(id); room.setAvailable(available); return roomRepository.save(room);
    }

    // Retrieves findById data for this API operation.
    public Room findById(Long id) {
        return roomRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Room not found."));
    }

    @Transactional
    // Creates or initiates create for the hotel workflow.
    public Room create(Room room) {
        if (roomRepository.findByRoomNumberIgnoreCase(room.getRoomNumber()).isPresent()) {
            throw new IllegalArgumentException("Room number already exists.");
        }
        room.setAvailable(true);
        return roomRepository.save(room);
    }

    @Transactional
    // Updates update after business-rule validation.
    public Room update(Long id, Room input) {
        Room room = findById(id);
        if (!room.getRoomNumber().equalsIgnoreCase(input.getRoomNumber())
            && roomRepository.findByRoomNumberIgnoreCase(input.getRoomNumber()).isPresent()) {
            throw new IllegalArgumentException("Room number already exists.");
        }
        room.setRoomNumber(input.getRoomNumber());
        room.setRoomType(input.getRoomType());
        room.setPricePerNight(input.getPricePerNight());
        room.setDescription(input.getDescription());
        room.setAvailable(input.isAvailable());
        return roomRepository.save(room);
    }

    @Transactional
    // Processes delete while preserving authorization and state rules.
    public void delete(Long id) {
        Room room = findById(id);
        roomRepository.delete(room);
    }
}
