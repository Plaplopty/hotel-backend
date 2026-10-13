package com.hotel.hotelreservation.model;

/** Reservation lifecycle, including manager-reviewed cancellation requests. */
public enum ReservationStatus { CONFIRMED, CANCELLATION_REQUESTED, CANCELLED, REJECTED }
