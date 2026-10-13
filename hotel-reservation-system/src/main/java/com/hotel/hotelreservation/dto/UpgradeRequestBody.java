package com.hotel.hotelreservation.dto;

/** Validated API request data transfer object. */

import jakarta.validation.constraints.NotNull;

public record UpgradeRequestBody(
    @NotNull Long reservationId,
    @NotNull Long requestedRoomId
) {}
