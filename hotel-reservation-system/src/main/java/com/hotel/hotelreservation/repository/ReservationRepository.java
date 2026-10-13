package com.hotel.hotelreservation.repository;

/** Spring Data repository for database access to the related domain entity. */

import com.hotel.hotelreservation.model.Reservation;
import com.hotel.hotelreservation.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByCustomerEmailIgnoreCaseOrderByCreatedAtDesc(String email);

    @Query("""
        select count(r) > 0 from Reservation r
        where r.room.id = :roomId
          and (r.status = :status or r.status = com.hotel.hotelreservation.model.ReservationStatus.CANCELLATION_REQUESTED)
          and r.checkIn < :checkOut
          and r.checkOut > :checkIn
        """)
    boolean hasOverlappingReservation(@Param("roomId") Long roomId,
                                     @Param("checkIn") LocalDate checkIn,
                                     @Param("checkOut") LocalDate checkOut,
                                     @Param("status") ReservationStatus status);
}
