package com.oceanview.resort.service;

import com.oceanview.resort.dao.ReservationDAO;
import com.oceanview.resort.dto.ReservationRequest;
import com.oceanview.resort.model.Guest;
import com.oceanview.resort.model.Reservation;
import com.oceanview.resort.model.Room;
import com.oceanview.resort.model.enums.ReservationStatus;
import com.oceanview.resort.repository.GuestRepository;
import com.oceanview.resort.repository.ReservationRepository;
import com.oceanview.resort.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Business Logic Layer for Reservation management.
 * Acts as mediator between UI and DAO layers, enforcing validation rules
 * and business rules before data persistence.
 * 
 * Depends on ReservationDAO interface (Dependency Inversion Principle).
 */
@Service
public class ReservationService {

    private final ReservationDAO reservationDAO;
    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final GuestRepository guestRepository;
    private final AtomicInteger reservationCounter = new AtomicInteger(1);

    public ReservationService(ReservationDAO reservationDAO,
            ReservationRepository reservationRepository,
            RoomRepository roomRepository,
            GuestRepository guestRepository) {
        this.reservationDAO = reservationDAO;
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
        this.guestRepository = guestRepository;
    }

    /**
     * Create a new reservation with full validation.
     * Auto-generates reservation ID in format: RES-YYYYMMDD-XXXX
     */
    @Transactional
    public Reservation createReservation(ReservationRequest request) {
        // Parse dates
        LocalDate checkIn = LocalDate.parse(request.getCheckInDate());
        LocalDate checkOut = LocalDate.parse(request.getCheckOutDate());

        // Validate dates
        if (checkIn.isBefore(LocalDate.now())) {
            throw new RuntimeException("Check-in date cannot be in the past");
        }
        if (checkOut.isBefore(checkIn) || checkOut.isEqual(checkIn)) {
            throw new RuntimeException("Check-out date must be after check-in date");
        }

        // Find room
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found with ID: " + request.getRoomId()));

        // Check room availability (prevent double booking)
        List<Reservation> conflicts = reservationRepository.findConflictingReservations(
                room.getRoomId(), checkIn, checkOut);
        if (!conflicts.isEmpty()) {
            throw new RuntimeException("Room " + room.getRoomNumber() +
                    " is not available for the selected dates. Please choose different dates or another room.");
        }

        // Create or find guest
        Guest guest = new Guest(request.getGuestName(), request.getGuestAddress(),
                request.getContactNumber(), request.getEmail());
        guest = guestRepository.save(guest);

        // Generate reservation ID
        String resId = generateReservationId();

        // Create reservation
        Reservation reservation = new Reservation(resId, guest, room, checkIn, checkOut);
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setTotalAmount(reservation.calculateBill());

        return reservationDAO.save(reservation);
    }

    /**
     * Find a reservation by its ID.
     */
    public Reservation findByReservationId(String reservationId) {
        return reservationDAO.findByReservationId(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found: " + reservationId));
    }

    /**
     * Get all reservations sorted by check-in date (descending).
     */
    public List<Reservation> getAllReservations() {
        return reservationDAO.findAll();
    }

    /**
     * Search reservations by keyword (guest name, contact, or reservation ID).
     */
    public List<Reservation> searchReservations(String keyword) {
        return reservationDAO.searchByKeyword(keyword);
    }

    /**
     * Cancel a reservation and update room availability.
     */
    @Transactional
    public Reservation cancelReservation(String reservationId) {
        Reservation reservation = findByReservationId(reservationId);

        if (reservation.getStatus() == ReservationStatus.CHECKED_OUT) {
            throw new RuntimeException("Cannot cancel a checked-out reservation");
        }
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new RuntimeException("Reservation is already cancelled");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        return reservationDAO.save(reservation);
    }

    /**
     * Update reservation status (e.g., PENDING -> CONFIRMED -> CHECKED_IN ->
     * CHECKED_OUT).
     */
    @Transactional
    public Reservation updateStatus(String reservationId, ReservationStatus newStatus) {
        Reservation reservation = findByReservationId(reservationId);
        reservation.setStatus(newStatus);
        return reservationDAO.save(reservation);
    }

    /**
     * Generate a unique reservation ID in the format RES-YYYYMMDD-XXXX.
     */
    private String generateReservationId() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int counter = reservationCounter.getAndIncrement();
        return String.format("RES-%s-%04d", datePart, counter);
    }
}
