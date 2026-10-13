package com.hotel.hotelreservation.repository;

/** Spring Data repository for database access to the related domain entity. */

import com.hotel.hotelreservation.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByRoomNumberIgnoreCase(String roomNumber);
}
