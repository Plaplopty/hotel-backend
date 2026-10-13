package com.hotel.hotelreservation;

/** Spring Boot application entry point. */

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HotelReservationSystemApplication {
    // Application entry point; starts the Spring Boot server.
    public static void main(String[] args) {
        SpringApplication.run(HotelReservationSystemApplication.class, args);
    }
}
