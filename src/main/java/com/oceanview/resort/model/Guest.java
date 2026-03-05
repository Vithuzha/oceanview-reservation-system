package com.oceanview.resort.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a hotel guest.
 * A guest can have multiple reservations over time (one-to-many relationship).
 */
@Entity
@Table(name = "guests")
public class Guest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long guestId;

    @NotBlank(message = "Guest name is required")
    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String address;

    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = "^[0-9+\\-\\s]{7,15}$", message = "Invalid contact number format")
    @Column(nullable = false, length = 15)
    private String contactNumber;

    @Column(length = 100)
    private String email;

    @OneToMany(mappedBy = "guest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Reservation> reservations = new ArrayList<>();

    // Default constructor
    public Guest() {
    }

    public Guest(String name, String address, String contactNumber, String email) {
        this.name = name;
        this.address = address;
        this.contactNumber = contactNumber;
        this.email = email;
    }

    // Getters and Setters
    public Long getGuestId() {
        return guestId;
    }

    public void setGuestId(Long guestId) {
        this.guestId = guestId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public List<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }
}
