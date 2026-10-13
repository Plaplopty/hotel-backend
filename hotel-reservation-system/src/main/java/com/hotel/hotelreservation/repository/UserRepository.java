package com.hotel.hotelreservation.repository;

/** Spring Data repository for database access to the related domain entity. */

import com.hotel.hotelreservation.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
}
