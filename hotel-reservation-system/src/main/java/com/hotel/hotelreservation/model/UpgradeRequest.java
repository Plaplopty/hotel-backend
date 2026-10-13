package com.hotel.hotelreservation.model;

/** JPA entity or enum used to persist hotel reservation domain data. */

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "upgrade_requests")
public class UpgradeRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @ManyToOne(optional = false)
    @JoinColumn(name = "requested_room_id", nullable = false)
    private Room requestedRoom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UpgradeStatus status = UpgradeStatus.PENDING;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    // Implements upgradeRequest for this class.
    public UpgradeRequest() {}

    // Retrieves getId data for this API operation.
    public Long getId() { return id; }
    // Retrieves getReservation data for this API operation.
    public Reservation getReservation() { return reservation; }
    // Updates setReservation after business-rule validation.
    public void setReservation(Reservation reservation) { this.reservation = reservation; }
    // Retrieves getRequestedRoom data for this API operation.
    public Room getRequestedRoom() { return requestedRoom; }
    // Updates setRequestedRoom after business-rule validation.
    public void setRequestedRoom(Room requestedRoom) { this.requestedRoom = requestedRoom; }
    // Retrieves getStatus data for this API operation.
    public UpgradeStatus getStatus() { return status; }
    // Updates setStatus after business-rule validation.
    public void setStatus(UpgradeStatus status) { this.status = status; }
    // Retrieves getCreatedAt data for this API operation.
    public Instant getCreatedAt() { return createdAt; }
}
