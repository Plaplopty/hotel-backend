package com.hotel.hotelreservation.repository;

/** Spring Data repository for database access to the related domain entity. */

import com.hotel.hotelreservation.model.UpgradeRequest;
import com.hotel.hotelreservation.model.UpgradeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UpgradeRequestRepository extends JpaRepository<UpgradeRequest, Long> {
    List<UpgradeRequest> findByStatusOrderByCreatedAtAsc(UpgradeStatus status);
    List<UpgradeRequest> findByReservationCustomerEmailIgnoreCaseOrderByCreatedAtDesc(String email);
}
