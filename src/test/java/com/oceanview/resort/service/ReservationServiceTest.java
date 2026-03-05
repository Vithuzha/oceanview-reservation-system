package com.oceanview.resort.service;

import com.oceanview.resort.dao.ReservationDAO;
import com.oceanview.resort.dto.ReservationRequest;
import com.oceanview.resort.model.*;
import com.oceanview.resort.model.enums.*;
import com.oceanview.resort.repository.GuestRepository;
import com.oceanview.resort.repository.ReservationRepository;
import com.oceanview.resort.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ReservationService.
 * Tests reservation creation, validation, search, and cancellation.
 */
@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationDAO reservationDAO;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private GuestRepository guestRepository;

    @InjectMocks
    private ReservationService reservationService;

    private Room testRoom;
    private Guest testGuest;

    @BeforeEach
    void setUp() {
        testRoom = new Room("101", RoomType.STANDARD, ViewType.OCEAN, 8000.0);
        testRoom.setRoomId(1L);

        testGuest = new Guest("John Doe", "123 Main St", "0771234567", "john@email.com");
        testGuest.setGuestId(1L);
    }

    @Test
    @DisplayName("Should create a reservation successfully")
    void createReservationSuccess() {
        // Arrange
        ReservationRequest request = createTestRequest();

        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(reservationRepository.findConflictingReservations(any(), any(), any()))
                .thenReturn(new ArrayList<>());
        when(guestRepository.save(any(Guest.class))).thenReturn(testGuest);
        when(reservationDAO.save(any(Reservation.class))).thenAnswer(i -> {
            Reservation res = i.getArgument(0);
            res.setId(1L);
            return res;
        });

        // Act
        Reservation result = reservationService.createReservation(request);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getReservationId());
        assertTrue(result.getReservationId().startsWith("RES-"));
        assertEquals(ReservationStatus.CONFIRMED, result.getStatus());
        assertEquals(testGuest, result.getGuest());
        assertEquals(testRoom, result.getRoom());
    }

    @Test
    @DisplayName("Should reject past check-in date")
    void rejectPastCheckIn() {
        ReservationRequest request = new ReservationRequest();
        request.setGuestName("John Doe");
        request.setContactNumber("0771234567");
        request.setRoomId(1L);
        request.setCheckInDate(LocalDate.now().minusDays(1).toString());
        request.setCheckOutDate(LocalDate.now().plusDays(2).toString());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> reservationService.createReservation(request));
        assertEquals("Check-in date cannot be in the past", ex.getMessage());
    }

    @Test
    @DisplayName("Should reject check-out before check-in")
    void rejectCheckOutBeforeCheckIn() {
        ReservationRequest request = new ReservationRequest();
        request.setGuestName("John Doe");
        request.setContactNumber("0771234567");
        request.setRoomId(1L);
        request.setCheckInDate(LocalDate.now().plusDays(5).toString());
        request.setCheckOutDate(LocalDate.now().plusDays(3).toString());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> reservationService.createReservation(request));
        assertEquals("Check-out date must be after check-in date", ex.getMessage());
    }

    @Test
    @DisplayName("Should prevent double booking")
    void preventDoubleBooking() {
        // Arrange
        ReservationRequest request = createTestRequest();
        List<Reservation> conflicts = new ArrayList<>();
        conflicts.add(new Reservation());

        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(reservationRepository.findConflictingReservations(any(), any(), any()))
                .thenReturn(conflicts);

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> reservationService.createReservation(request));
        assertTrue(ex.getMessage().contains("not available"));
    }

    @Test
    @DisplayName("Should find reservation by ID")
    void findByReservationId() {
        Reservation expected = new Reservation("RES-20260303-0001", testGuest, testRoom,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        when(reservationDAO.findByReservationId("RES-20260303-0001"))
                .thenReturn(Optional.of(expected));

        Reservation result = reservationService.findByReservationId("RES-20260303-0001");
        assertNotNull(result);
        assertEquals("RES-20260303-0001", result.getReservationId());
    }

    @Test
    @DisplayName("Should throw error for non-existent reservation")
    void findNonExistentReservation() {
        when(reservationDAO.findByReservationId("RES-INVALID"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> reservationService.findByReservationId("RES-INVALID"));
    }

    @Test
    @DisplayName("Should cancel a confirmed reservation")
    void cancelReservation() {
        Reservation reservation = new Reservation("RES-20260303-0001", testGuest, testRoom,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        reservation.setStatus(ReservationStatus.CONFIRMED);

        when(reservationDAO.findByReservationId("RES-20260303-0001"))
                .thenReturn(Optional.of(reservation));
        when(reservationDAO.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        Reservation result = reservationService.cancelReservation("RES-20260303-0001");
        assertEquals(ReservationStatus.CANCELLED, result.getStatus());
    }

    @Test
    @DisplayName("Should not cancel already cancelled reservation")
    void cancelAlreadyCancelled() {
        Reservation reservation = new Reservation("RES-20260303-0001", testGuest, testRoom,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        reservation.setStatus(ReservationStatus.CANCELLED);

        when(reservationDAO.findByReservationId("RES-20260303-0001"))
                .thenReturn(Optional.of(reservation));

        assertThrows(RuntimeException.class,
                () -> reservationService.cancelReservation("RES-20260303-0001"));
    }

    @Test
    @DisplayName("Should search reservations by keyword")
    void searchReservations() {
        List<Reservation> expected = new ArrayList<>();
        expected.add(new Reservation("RES-20260303-0001", testGuest, testRoom,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3)));

        when(reservationDAO.searchByKeyword("John")).thenReturn(expected);

        List<Reservation> results = reservationService.searchReservations("John");
        assertEquals(1, results.size());
    }

    private ReservationRequest createTestRequest() {
        ReservationRequest request = new ReservationRequest();
        request.setGuestName("John Doe");
        request.setGuestAddress("123 Main St");
        request.setContactNumber("0771234567");
        request.setEmail("john@email.com");
        request.setRoomId(1L);
        request.setCheckInDate(LocalDate.now().plusDays(1).toString());
        request.setCheckOutDate(LocalDate.now().plusDays(4).toString());
        return request;
    }
}
