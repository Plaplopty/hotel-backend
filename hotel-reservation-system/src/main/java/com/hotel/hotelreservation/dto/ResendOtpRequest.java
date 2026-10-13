package com.hotel.hotelreservation.dto;

/** Validated API request data transfer object. */

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendOtpRequest(@NotBlank @Email String email, String captchaToken) {}
