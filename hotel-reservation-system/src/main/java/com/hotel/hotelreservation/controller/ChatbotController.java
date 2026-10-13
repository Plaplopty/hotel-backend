package com.hotel.hotelreservation.controller;

/** REST API endpoints for the related hotel reservation feature. */

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {
    public record ChatMessage(@NotBlank @Size(max = 500) String message) {}

    @PostMapping
    // Implements reply for this class.
    public Map<String, String> reply(@Valid @RequestBody ChatMessage input) {
        String message = input.message().toLowerCase(Locale.ROOT);
        String reply;
        if (message.contains("check-in") || message.contains("check in")) {
            reply = "Please contact the hotel manager for the current check-in time.";
        } else if (message.contains("check-out") || message.contains("check out")) {
            reply = "Please contact the hotel manager for the current check-out time.";
        } else if (message.contains("upgrade")) {
            reply = "Log in and submit an upgrade request from your reservation. Approval depends on availability.";
        } else if (message.contains("cancel")) {
            reply = "Log in and open your reservations to cancel a confirmed reservation.";
        } else if (message.contains("room") || message.contains("price") || message.contains("availability")) {
            reply = "You can browse available room information at GET /api/rooms.";
        } else {
            reply = "I can help with rooms, availability, reservations, cancellations, and upgrades. For other questions, contact the hotel manager.";
        }
        return Map.of("reply", reply);
    }
}
