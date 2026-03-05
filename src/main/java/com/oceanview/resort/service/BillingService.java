package com.oceanview.resort.service;

import com.oceanview.resort.dto.BillResponse;
import com.oceanview.resort.model.Bill;
import com.oceanview.resort.model.Reservation;
import com.oceanview.resort.repository.BillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for Billing operations.
 * Handles bill calculation, generation, and retrieval.
 */
@Service
public class BillingService {

    private final BillRepository billRepository;
    private final ReservationService reservationService;

    public BillingService(BillRepository billRepository, ReservationService reservationService) {
        this.billRepository = billRepository;
        this.reservationService = reservationService;
    }

    /**
     * Generate a bill for a reservation.
     * Creates a new Bill object if one doesn't exist, or returns the existing one.
     */
    @Transactional
    public BillResponse generateBill(String reservationId) {
        Reservation reservation = reservationService.findByReservationId(reservationId);

        // Check if bill already exists
        Bill bill = billRepository.findByReservationReservationId(reservationId)
                .orElseGet(() -> {
                    Bill newBill = new Bill(reservation);
                    return billRepository.save(newBill);
                });

        return mapToBillResponse(bill, reservation);
    }

    /**
     * Retrieve an existing bill for a reservation.
     */
    public BillResponse getBill(String reservationId) {
        Bill bill = billRepository.findByReservationReservationId(reservationId)
                .orElseThrow(() -> new RuntimeException("Bill not found for reservation: " + reservationId));

        Reservation reservation = reservationService.findByReservationId(reservationId);
        return mapToBillResponse(bill, reservation);
    }

    /**
     * Map Bill entity to BillResponse DTO.
     */
    private BillResponse mapToBillResponse(Bill bill, Reservation reservation) {
        BillResponse response = new BillResponse();
        response.setReservationId(reservation.getReservationId());
        response.setGuestName(reservation.getGuest().getName());
        response.setRoomNumber(reservation.getRoom().getRoomNumber());
        response.setRoomType(reservation.getRoom().getRoomType().name());
        response.setCheckInDate(reservation.getCheckInDate().toString());
        response.setCheckOutDate(reservation.getCheckOutDate().toString());
        response.setNights(bill.getNights());
        response.setRoomRate(bill.getRoomRate());
        response.setSubtotal(bill.getSubtotal());
        response.setTaxRate(Bill.TAX_RATE * 100);
        response.setTaxAmount(bill.getTaxAmount());
        response.setTotalAmount(bill.getTotalAmount());
        return response;
    }
}
