package com.hotel.hotelreservation.controller;

/** REST API endpoints for the related hotel reservation feature. */

import com.hotel.hotelreservation.dto.*;
import com.hotel.hotelreservation.service.AuthService;
import com.hotel.hotelreservation.service.OtpService;
import com.hotel.hotelreservation.service.ResendOtpService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final OtpService otpService;
    private final ResendOtpService resendOtpService;

    // Implements authController for this class.
    public AuthController(AuthService authService, OtpService otpService, ResendOtpService resendOtpService) {
        this.authService = authService;
        this.otpService = otpService;
        this.resendOtpService = resendOtpService;
    }

    @PostMapping("/register")
    // Creates or initiates register for the hotel workflow.
    public Map<String, String> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest servletRequest) {
        return authService.register(request, servletRequest);
    }

    @PostMapping("/verify-email")
    // Validates verifyEmail and enforces the relevant security checks.
    public Map<String, String> verifyEmail(@Valid @RequestBody VerifyOtpRequest request) {
        otpService.verify(request.email(), request.code());
        return Map.of("message", "Email verified. You can now log in.");
    }

    @PostMapping("/resend-otp")
    // Creates or initiates resendOtp for the hotel workflow.
    public Map<String, String> resendOtp(@Valid @RequestBody ResendOtpRequest request, HttpServletRequest servletRequest) {
        return resendOtpService.resend(request, servletRequest);
    }

    /** Verifies the email OTP issued after password login and returns a JWT. */
    @PostMapping("/verify-login")
    // Validates verifyLogin and enforces the relevant security checks.
    public Map<String, String> verifyLogin(@Valid @RequestBody VerifyOtpRequest request) {
        return authService.verifyLogin(request.email(), request.code());
    }

    /** Starts the password + CAPTCHA login stage; successful credentials trigger email MFA. */
    @PostMapping("/login")
    // Validates login and enforces the relevant security checks.
    public Map<String, String> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        return authService.login(request, servletRequest);
    }
}
