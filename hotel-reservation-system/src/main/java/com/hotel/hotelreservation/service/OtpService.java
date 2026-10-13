package com.hotel.hotelreservation.service;

/** Business logic for the hotel reservation workflow. */

import com.hotel.hotelreservation.model.User;
import com.hotel.hotelreservation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, OtpEntry> codes = new ConcurrentHashMap<>();
    // Separate one-time codes for login MFA; verifying one never verifies the account email.
    private final Map<String, OtpEntry> loginCodes = new ConcurrentHashMap<>();

    @Value("${app.otp.expiration-minutes:10}")
    private long expirationMinutes;
    @Value("${app.otp.max-attempts:5}")
    private int maxAttempts;
    @Value("${spring.mail.username:}")
    private String fromAddress;

    // Implements otpService for this class.
    public OtpService(JavaMailSender mailSender, UserRepository userRepository) {
        this.mailSender = mailSender;
        this.userRepository = userRepository;
    }

    // Creates or initiates sendVerificationCode for the hotel workflow.
    public void sendVerificationCode(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new IllegalArgumentException("Unable to send verification code."));
        if (user.isEmailVerified()) {
            throw new IllegalArgumentException("Unable to send verification code.");
        }

        String code = String.format("%06d", secureRandom.nextInt(1_000_000));
        codes.put(email.toLowerCase(), new OtpEntry(code, Instant.now().plusSeconds(expirationMinutes * 60), 0));

        if (fromAddress == null || fromAddress.isBlank()) {
            codes.remove(email.toLowerCase());
            throw new IllegalStateException("Email service is not configured. Set MAIL_HOST, MAIL_USERNAME, and MAIL_PASSWORD.");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(email);
        message.setSubject("Hotel account verification code");
        message.setText("Your verification code is " + code + ". It expires in " + expirationMinutes
            + " minutes. If you did not request this, you can ignore this email.");
        try {
            mailSender.send(message);
        } catch (Exception ex) {
            codes.remove(email.toLowerCase());
            throw new IllegalStateException("Unable to send verification email. Check your mail settings.");
        }
    }

    /** Sends the second-factor code after a valid password and CAPTCHA. */
    public void sendLoginCode(String email) { sendCode(email, loginCodes, "Hotel login verification code"); }

    /** Validates and consumes the login MFA code with expiry and attempt limits. */
    public void verifyLoginCode(String email, String code) { verifyCode(email, code, loginCodes); }

    // Creates or initiates sendCode for the hotel workflow.
    private void sendCode(String email, Map<String, OtpEntry> store, String subject) {
        String key = email.toLowerCase(java.util.Locale.ROOT);
        String code = String.format("%06d", secureRandom.nextInt(1_000_000));
        store.put(key, new OtpEntry(code, Instant.now().plusSeconds(expirationMinutes * 60), 0));
        if (fromAddress == null || fromAddress.isBlank()) {
            store.remove(key);
            throw new IllegalStateException("Email service is not configured. Set MAIL_HOST, MAIL_USERNAME, and MAIL_PASSWORD.");
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress); message.setTo(email); message.setSubject(subject);
        message.setText("Your one-time code is " + code + ". It expires in " + expirationMinutes + " minutes.");
        try { mailSender.send(message); } catch (Exception ex) {
            store.remove(key);
            throw new IllegalStateException("Unable to send verification email. Check your mail settings.");
        }
    }

    @Transactional
    // Validates verify and enforces the relevant security checks.
    public void verify(String email, String code) {
        verifyCode(email, code, codes);
        User user = userRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new IllegalArgumentException("Invalid or expired verification code."));
        user.setEmailVerified(true);
        userRepository.save(user);
    }

    // Validates verifyCode and enforces the relevant security checks.
    private void verifyCode(String email, String code, Map<String, OtpEntry> store) {
        String key = email.toLowerCase(java.util.Locale.ROOT);
        OtpEntry entry = store.get(key);
        if (entry == null) throw new IllegalArgumentException("Invalid or expired verification code.");
        if (!Instant.now().isBefore(entry.expiresAt())) {
            store.remove(key);
            throw new IllegalArgumentException("Invalid or expired verification code.");
        }
        if (entry.attempts() >= maxAttempts) {
            store.remove(key);
            throw new IllegalArgumentException("Too many attempts. Request a new code.");
        }
        if (!constantTimeEquals(entry.code(), code)) {
            store.put(key, new OtpEntry(entry.code(), entry.expiresAt(), entry.attempts() + 1));
            throw new IllegalArgumentException("Invalid or expired verification code.");
        }
        store.remove(key);
    }

    // Helper function that constantTimeEquals for the calling workflow.
    private boolean constantTimeEquals(String expected, String actual) {
        if (actual == null) return false;
        return java.security.MessageDigest.isEqual(
            expected.getBytes(java.nio.charset.StandardCharsets.UTF_8),
            actual.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private record OtpEntry(String code, Instant expiresAt, int attempts) {}
}
