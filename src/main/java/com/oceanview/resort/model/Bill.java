package com.oceanview.resort.model;

import jakarta.persistence.*;

/**
 * Entity encapsulating billing logic.
 * A Bill has a composition relationship with Reservation (cannot exist
 * independently).
 * Tax rate: 15% as per system assumptions (Sri Lanka VAT + tourism tax).
 */
@Entity
@Table(name = "bills")
public class Bill {

    public static final double TAX_RATE = 0.15;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long billId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reservation_id", nullable = false, unique = true)
    private Reservation reservation;

    @Column(nullable = false)
    private int nights;

    @Column(nullable = false)
    private double roomRate;

    @Column(nullable = false)
    private double subtotal;

    @Column(nullable = false)
    private double taxAmount;

    @Column(nullable = false)
    private double totalAmount;

    // Default constructor
    public Bill() {
    }

    /**
     * Creates a Bill from a Reservation.
     * Calculates nights, subtotal, tax, and total automatically.
     */
    public Bill(Reservation reservation) {
        this.reservation = reservation;
        this.nights = reservation.calculateNights();
        this.roomRate = reservation.getRoom().getPricePerNight();
        this.subtotal = this.nights * this.roomRate;
        this.taxAmount = this.subtotal * TAX_RATE;
        this.totalAmount = this.subtotal + this.taxAmount;
    }

    // Getters and Setters
    public Long getBillId() {
        return billId;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public int getNights() {
        return nights;
    }

    public void setNights(int nights) {
        this.nights = nights;
    }

    public double getRoomRate() {
        return roomRate;
    }

    public void setRoomRate(double roomRate) {
        this.roomRate = roomRate;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(double taxAmount) {
        this.taxAmount = taxAmount;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
}
