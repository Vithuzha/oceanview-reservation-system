package com.oceanview.resort.model;

import com.oceanview.resort.model.enums.RoomType;
import com.oceanview.resort.model.enums.ViewType;
import jakarta.persistence.*;

/**
 * Entity representing a physical hotel room.
 * Each room has a type (Standard, Deluxe, Suite), a view type, and nightly
 * pricing.
 */
@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomId;

    @Column(nullable = false, unique = true, length = 10)
    private String roomNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomType roomType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ViewType viewType;

    @Column(nullable = false)
    private double pricePerNight;

    @Column(nullable = false)
    private boolean isAvailable = true;

    @Column(length = 255)
    private String description;

    // Default constructor
    public Room() {
    }

    public Room(String roomNumber, RoomType roomType, ViewType viewType, double pricePerNight) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.viewType = viewType;
        this.pricePerNight = pricePerNight;
        this.isAvailable = true;
    }

    // Getters and Setters
    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public ViewType getViewType() {
        return viewType;
    }

    public void setViewType(ViewType viewType) {
        this.viewType = viewType;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(double pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
