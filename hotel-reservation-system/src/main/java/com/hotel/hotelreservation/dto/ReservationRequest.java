package com.hotel.hotelreservation.dto;

/** Validated API request data transfer object. */

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ReservationRequest(
    @NotNull Long roomId,
    @NotNull LocalDate checkIn,
    @NotNull LocalDate checkOut
) {}
