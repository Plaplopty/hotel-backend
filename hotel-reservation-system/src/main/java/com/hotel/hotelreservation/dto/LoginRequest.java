package com.hotel.hotelreservation.dto;

/** Validated API request data transfer object. */

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank @Email String email,
    @NotBlank String password,
    String captchaToken
) {}
