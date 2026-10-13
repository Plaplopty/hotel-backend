package com.hotel.hotelreservation.model;

/** JPA entity or enum used to persist hotel reservation domain data. */

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.Instant;

@Entity
@Table(name = "reservations")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private LocalDate checkIn;

    @Column(nullable = false)
    private LocalDate checkOut;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status = ReservationStatus.CONFIRMED;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    // Implements reservation for this class.
    public Reservation() {}

    // Retrieves getId data for this API operation.
    public Long getId() { return id; }
    // Retrieves getCustomer data for this API operation.
    public User getCustomer() { return customer; }
    // Updates setCustomer after business-rule validation.
    public void setCustomer(User customer) { this.customer = customer; }
    // Retrieves getRoom data for this API operation.
    public Room getRoom() { return room; }
    // Updates setRoom after business-rule validation.
    public void setRoom(Room room) { this.room = room; }
    // Retrieves getCheckIn data for this API operation.
    public LocalDate getCheckIn() { return checkIn; }
    // Updates setCheckIn after business-rule validation.
    public void setCheckIn(LocalDate checkIn) { this.checkIn = checkIn; }
    // Retrieves getCheckOut data for this API operation.
    public LocalDate getCheckOut() { return checkOut; }
    // Updates setCheckOut after business-rule validation.
    public void setCheckOut(LocalDate checkOut) { this.checkOut = checkOut; }
    // Retrieves getStatus data for this API operation.
    public ReservationStatus getStatus() { return status; }
    // Updates setStatus after business-rule validation.
    public void setStatus(ReservationStatus status) { this.status = status; }
    // Retrieves getCreatedAt data for this API operation.
    public Instant getCreatedAt() { return createdAt; }
}
