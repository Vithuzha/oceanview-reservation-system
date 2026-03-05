package com.oceanview.resort.model.enums;

/**
 * Enumeration representing the lifecycle status of a reservation.
 * PENDING -> CONFIRMED -> CHECKED_IN -> CHECKED_OUT
 * A reservation can be CANCELLED at any point before CHECKED_OUT.
 */
public enum ReservationStatus {
    PENDING,
    CONFIRMED,
    CHECKED_IN,
    CHECKED_OUT,
    CANCELLED
}
