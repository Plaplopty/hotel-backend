package com.hotel.hotelreservation.service;

/** Business logic for the hotel reservation workflow. */

import com.hotel.hotelreservation.dto.ResendOtpRequest;
import com.hotel.hotelreservation.security.CaptchaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ResendOtpService {
    private final CaptchaService captchaService;
    private final OtpService otpService;

    // Implements resendOtpService for this class.
    public ResendOtpService(CaptchaService captchaService, OtpService otpService) {
        this.captchaService = captchaService;
        this.otpService = otpService;
    }

    // Creates or initiates resend for the hotel workflow.
    public Map<String, String> resend(ResendOtpRequest request, HttpServletRequest servletRequest) {
        if (!captchaService.isValid(request.captchaToken(), servletRequest.getRemoteAddr())) {
            throw new IllegalArgumentException("CAPTCHA verification failed.");
        }
        otpService.sendVerificationCode(request.email().trim().toLowerCase());
        return Map.of("message", "If the account can be verified, a code will be sent.");
    }
}
