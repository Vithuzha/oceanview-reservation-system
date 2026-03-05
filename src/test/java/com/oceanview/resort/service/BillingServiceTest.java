package com.oceanview.resort.service;

import com.oceanview.resort.dto.BillResponse;
import com.oceanview.resort.model.*;
import com.oceanview.resort.model.enums.*;
import com.oceanview.resort.repository.BillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BillingService.
 * Tests bill calculation, tax computation, and bill generation.
 */
@ExtendWith(MockitoExtension.class)
class BillingServiceTest {

    @Mock
    private BillRepository billRepository;

    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private BillingService billingService;

    private Reservation testReservation;
    private Room testRoom;
    private Guest testGuest;

    @BeforeEach
    void setUp() {
        testGuest = new Guest("John Doe", "123 Main St", "0771234567", "john@email.com");
        testGuest.setGuestId(1L);

        testRoom = new Room("101", RoomType.STANDARD, ViewType.OCEAN, 8000.0);
        testRoom.setRoomId(1L);

        testReservation = new Reservation("RES-20260303-0001", testGuest, testRoom,
                LocalDate.of(2026, 3, 5), LocalDate.of(2026, 3, 8));
        testReservation.setId(1L);
        testReservation.setStatus(ReservationStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Should calculate correct number of nights")
    void calculateNights() {
        // 3 nights: March 5 to March 8
        assertEquals(3, testReservation.calculateNights());
    }

    @Test
    @DisplayName("Should calculate correct bill amount with 15% tax")
    void calculateBillAmount() {
        // 3 nights × 8000 LKR = 24000 + 15% tax (3600) = 27600
        double expectedTotal = 3 * 8000.0 * 1.15;
        assertEquals(expectedTotal, testReservation.calculateBill(), 0.01);
    }

    @Test
    @DisplayName("Should generate bill object with correct values")
    void generateBillObject() {
        Bill bill = new Bill(testReservation);

        assertEquals(3, bill.getNights());
        assertEquals(8000.0, bill.getRoomRate());
        assertEquals(24000.0, bill.getSubtotal(), 0.01);
        assertEquals(3600.0, bill.getTaxAmount(), 0.01); // 15% of 24000
        assertEquals(27600.0, bill.getTotalAmount(), 0.01);
    }

    @Test
    @DisplayName("Should generate bill via service and return BillResponse")
    void generateBillViaService() {
        // Arrange
        when(reservationService.findByReservationId("RES-20260303-0001")).thenReturn(testReservation);
        when(billRepository.findByReservationReservationId("RES-20260303-0001")).thenReturn(Optional.empty());
        when(billRepository.save(any(Bill.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        BillResponse response = billingService.generateBill("RES-20260303-0001");

        // Assert
        assertNotNull(response);
        assertEquals("RES-20260303-0001", response.getReservationId());
        assertEquals("John Doe", response.getGuestName());
        assertEquals("101", response.getRoomNumber());
        assertEquals(3, response.getNights());
        assertEquals(8000.0, response.getRoomRate());
        assertEquals(24000.0, response.getSubtotal(), 0.01);
        assertEquals(15.0, response.getTaxRate());
        assertEquals(3600.0, response.getTaxAmount(), 0.01);
        assertEquals(27600.0, response.getTotalAmount(), 0.01);
    }

    @Test
    @DisplayName("Should return existing bill if already generated")
    void returnExistingBill() {
        // Arrange
        Bill existingBill = new Bill(testReservation);
        when(reservationService.findByReservationId("RES-20260303-0001")).thenReturn(testReservation);
        when(billRepository.findByReservationReservationId("RES-20260303-0001"))
                .thenReturn(Optional.of(existingBill));

        // Act
        BillResponse response = billingService.generateBill("RES-20260303-0001");

        // Assert
        assertNotNull(response);
        verify(billRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deluxe room billing should use correct rate")
    void deluxeRoomBilling() {
        // Arrange
        Room deluxeRoom = new Room("201", RoomType.DELUXE, ViewType.OCEAN, 14000.0);
        Reservation deluxeRes = new Reservation("RES-20260303-0002", testGuest, deluxeRoom,
                LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 12));

        // 2 nights × 14000 = 28000 + 15% tax (4200) = 32200
        Bill bill = new Bill(deluxeRes);
        assertEquals(2, bill.getNights());
        assertEquals(14000.0, bill.getRoomRate());
        assertEquals(28000.0, bill.getSubtotal(), 0.01);
        assertEquals(4200.0, bill.getTaxAmount(), 0.01);
        assertEquals(32200.0, bill.getTotalAmount(), 0.01);
    }

    @Test
    @DisplayName("Suite room billing should use correct rate")
    void suiteRoomBilling() {
        Room suiteRoom = new Room("301", RoomType.SUITE, ViewType.OCEAN, 25000.0);
        Reservation suiteRes = new Reservation("RES-20260303-0003", testGuest, suiteRoom,
                LocalDate.of(2026, 3, 15), LocalDate.of(2026, 3, 20));

        // 5 nights × 25000 = 125000 + 15% tax (18750) = 143750
        Bill bill = new Bill(suiteRes);
        assertEquals(5, bill.getNights());
        assertEquals(125000.0, bill.getSubtotal(), 0.01);
        assertEquals(18750.0, bill.getTaxAmount(), 0.01);
        assertEquals(143750.0, bill.getTotalAmount(), 0.01);
    }
}
