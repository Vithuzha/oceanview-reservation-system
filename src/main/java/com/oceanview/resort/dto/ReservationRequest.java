package com.oceanview.resort.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Data Transfer Object for creating new reservations.
 */
public class ReservationRequest {

    @NotBlank(message = "Guest name is required")
    private String guestName;

    private String guestAddress;

    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = "^[0-9+\\-\\s]{7,15}$", message = "Invalid contact number format")
    private String contactNumber;

    private String email;

    @NotNull(message = "Room ID is required")
    private Long roomId;

    @NotBlank(message = "Check-in date is required")
    private String checkInDate;

    @NotBlank(message = "Check-out date is required")
    private String checkOutDate;

    // Getters and Setters
    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getGuestAddress() {
        return guestAddress;
    }

    public void setGuestAddress(String guestAddress) {
        this.guestAddress = guestAddress;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(String checkInDate) {
        this.checkInDate = checkInDate;
    }

    public String getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(String checkOutDate) {
        this.checkOutDate = checkOutDate;
    }
}
