package com.hotel.hotelreservation.controller;

/** REST API endpoints for the related hotel reservation feature. */

import com.hotel.hotelreservation.model.User;
import com.hotel.hotelreservation.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
@PreAuthorize("hasRole('MANAGER')")
public class CustomerController {
    private final UserRepository userRepository;

    // Implements customerController for this class.
    public CustomerController(UserRepository userRepository) { this.userRepository = userRepository; }

    @GetMapping
    // Retrieves all data for this API operation.
    public List<Map<String, Object>> all() {
        return userRepository.findAll().stream().map(this::safeView).toList();
    }

    @GetMapping("/{id}")
    // Retrieves one data for this API operation.
    public Map<String, Object> one(@PathVariable Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Customer not found."));
        return safeView(user);
    }

    /** Request payload for manager edits; role and password are intentionally not editable here. */
    public record CustomerUpdate(@NotBlank @Size(max = 100) String name,
                                 @NotBlank @Email @Size(max = 180) String email) {}

    /** Updates a customer's display name and email, enforcing unique email addresses. */
    @PutMapping("/{id}")
    // Updates update after business-rule validation.
    public Map<String, Object> update(@PathVariable Long id, @Valid @RequestBody CustomerUpdate input) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found."));
        String email = input.email().trim().toLowerCase(java.util.Locale.ROOT);
        userRepository.findByEmailIgnoreCase(email).ifPresent(existing -> {
            if (!existing.getId().equals(id)) throw new IllegalArgumentException("Email is already in use.");
        });
        user.setName(input.name().trim());
        user.setEmail(email);
        user.setEmailVerified(false); // New address must be verified before a subsequent login.
        return safeView(userRepository.save(user));
    }

    // Helper function that safeView for the calling workflow.
    private Map<String, Object> safeView(User user) {
        return Map.of(
            "id", user.getId(),
            "name", user.getName(),
            "email", user.getEmail(),
            "role", user.getRole().name(),
            "emailVerified", user.isEmailVerified(),
            "createdAt", user.getCreatedAt().toString()
        );
    }
}
