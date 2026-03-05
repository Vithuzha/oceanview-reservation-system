package com.oceanview.resort.model;

import com.oceanview.resort.model.enums.ReservationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Core booking entity that manages the entire lifecycle of a reservation.
 * Reservation IDs are auto-generated in the format RES-YYYYMMDD-XXXX.
 */
@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String reservationId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "guest_id", nullable = false)
    private Guest guest;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @NotNull(message = "Check-in date is required")
    @Column(nullable = false)
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is required")
    @Column(nullable = false)
    private LocalDate checkOutDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status = ReservationStatus.PENDING;

    @Column(nullable = false)
    private double totalAmount;

    @OneToOne(mappedBy = "reservation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Bill bill;

    // Default constructor
    public Reservation() {
    }

    public Reservation(String reservationId, Guest guest, Room room,
            LocalDate checkInDate, LocalDate checkOutDate) {
        this.reservationId = reservationId;
        this.guest = guest;
        this.room = room;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.status = ReservationStatus.PENDING;
    }

    /**
     * Calculate the number of nights between check-in and check-out.
     * Uses Java's LocalDate.until() method as specified in the design.
     */
    public int calculateNights() {
        if (checkInDate != null && checkOutDate != null) {
            return (int) ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        }
        return 0;
    }

    /**
     * Calculate the total bill amount.
     * Formula: nights × pricePerNight × (1 + taxRate)
     * Tax rate is 15% as per system assumptions.
     */
    public double calculateBill() {
        int nights = calculateNights();
        double roomRate = room != null ? room.getPricePerNight() : 0;
        double subtotal = nights * roomRate;
        double tax = subtotal * 0.15;
        return subtotal + tax;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public Guest getGuest() {
        return guest;
    }

    public void setGuest(Guest guest) {
        this.guest = guest;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Bill getBill() {
        return bill;
    }

    public void setBill(Bill bill) {
        this.bill = bill;
    }
}
