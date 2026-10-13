package com.hotel.hotelreservation.model;

/** JPA entity or enum used to persist hotel reservation domain data. */

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 180)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.CUSTOMER;

    @Column(nullable = false)
    private boolean emailVerified = false;

    private Instant createdAt = Instant.now();

    // Implements user for this class.
    public User() {}

    // Retrieves getId data for this API operation.
    public Long getId() { return id; }
    // Retrieves getName data for this API operation.
    public String getName() { return name; }
    // Updates setName after business-rule validation.
    public void setName(String name) { this.name = name; }
    // Retrieves getEmail data for this API operation.
    public String getEmail() { return email; }
    // Updates setEmail after business-rule validation.
    public void setEmail(String email) { this.email = email; }
    // Retrieves getPasswordHash data for this API operation.
    public String getPasswordHash() { return passwordHash; }
    // Updates setPasswordHash after business-rule validation.
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    // Retrieves getRole data for this API operation.
    public Role getRole() { return role; }
    // Updates setRole after business-rule validation.
    public void setRole(Role role) { this.role = role; }
    // Helper function that isEmailVerified for the calling workflow.
    public boolean isEmailVerified() { return emailVerified; }
    // Updates setEmailVerified after business-rule validation.
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }
    // Retrieves getCreatedAt data for this API operation.
    public Instant getCreatedAt() { return createdAt; }
}
