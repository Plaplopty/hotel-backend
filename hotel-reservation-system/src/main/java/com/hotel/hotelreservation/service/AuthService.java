package com.hotel.hotelreservation.service;

/** Business logic for the hotel reservation workflow. */

import com.hotel.hotelreservation.dto.LoginRequest;
import com.hotel.hotelreservation.dto.RegisterRequest;
import com.hotel.hotelreservation.model.Role;
import com.hotel.hotelreservation.model.User;
import com.hotel.hotelreservation.repository.UserRepository;
import com.hotel.hotelreservation.security.CaptchaService;
import com.hotel.hotelreservation.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final CaptchaService captchaService;
    private final JwtService jwtService;

    // Implements authService for this class.
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       OtpService otpService, CaptchaService captchaService, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
        this.captchaService = captchaService;
        this.jwtService = jwtService;
    }

    @Transactional
    // Creates or initiates register for the hotel workflow.
    public Map<String, String> register(RegisterRequest request, HttpServletRequest servletRequest) {
        if (!captchaService.isValid(request.captchaToken(), servletRequest.getRemoteAddr())) {
            throw new IllegalArgumentException("CAPTCHA verification failed.");
        }
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Unable to register with those details.");
        }
        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.CUSTOMER);
        user.setEmailVerified(false);
        userRepository.save(user);
        otpService.sendVerificationCode(email);
        return Map.of("message", "Account created. Check your email for the verification code.");
    }

    // Validates login and enforces the relevant security checks.
    public Map<String, String> login(LoginRequest request, HttpServletRequest servletRequest) {
        if (!captchaService.isValid(request.captchaToken(), servletRequest.getRemoteAddr())) {
            throw new IllegalArgumentException("CAPTCHA verification failed.");
        }
        User user = userRepository.findByEmailIgnoreCase(request.email().trim())
            .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }
        if (!user.isEmailVerified()) {
            throw new IllegalArgumentException("Please verify your email before logging in.");
        }
        // Password alone never grants a session: the client must complete email OTP MFA.
        otpService.sendLoginCode(user.getEmail());
        return Map.of("message", "A login code was sent to your email.", "mfaRequired", "true");
    }

    /** Completes login MFA and issues the short-lived bearer JWT. */
    public Map<String, String> verifyLogin(String email, String code) {
        User user = userRepository.findByEmailIgnoreCase(email.trim().toLowerCase(java.util.Locale.ROOT))
            .orElseThrow(() -> new IllegalArgumentException("Invalid or expired verification code."));
        if (!user.isEmailVerified()) throw new IllegalArgumentException("Please verify your email before logging in.");
        otpService.verifyLoginCode(user.getEmail(), code);
        return Map.of("token", jwtService.createToken(user.getEmail(), user.getRole().name()),
            "tokenType", "Bearer", "role", user.getRole().name());
    }
}
