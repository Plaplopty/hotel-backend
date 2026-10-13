package com.hotel.hotelreservation.model;

/** JPA entity or enum used to persist hotel reservation domain data. */

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Entity
@Table(name = "rooms", uniqueConstraints = @UniqueConstraint(columnNames = "roomNumber"))
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 20)
    @Column(nullable = false, unique = true, length = 20)
    private String roomNumber;

    @NotBlank
    @Size(max = 60)
    @Column(nullable = false, length = 60)
    private String roomType;

    @DecimalMin(value = "0.01")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerNight;

    @Column(nullable = false)
    private boolean available = true;

    @Column(length = 500)
    private String description;

    // Implements room for this class.
    public Room() {}

    // Retrieves getId data for this API operation.
    public Long getId() { return id; }
    // Retrieves getRoomNumber data for this API operation.
    public String getRoomNumber() { return roomNumber; }
    // Updates setRoomNumber after business-rule validation.
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    // Retrieves getRoomType data for this API operation.
    public String getRoomType() { return roomType; }
    // Updates setRoomType after business-rule validation.
    public void setRoomType(String roomType) { this.roomType = roomType; }
    // Retrieves getPricePerNight data for this API operation.
    public BigDecimal getPricePerNight() { return pricePerNight; }
    // Updates setPricePerNight after business-rule validation.
    public void setPricePerNight(BigDecimal pricePerNight) { this.pricePerNight = pricePerNight; }
    // Helper function that isAvailable for the calling workflow.
    public boolean isAvailable() { return available; }
    // Updates setAvailable after business-rule validation.
    public void setAvailable(boolean available) { this.available = available; }
    // Retrieves getDescription data for this API operation.
    public String getDescription() { return description; }
    // Updates setDescription after business-rule validation.
    public void setDescription(String description) { this.description = description; }
}
