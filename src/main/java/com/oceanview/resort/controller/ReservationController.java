package com.oceanview.resort.controller;

import com.oceanview.resort.dto.ReservationRequest;
import com.oceanview.resort.model.Reservation;
import com.oceanview.resort.model.enums.ReservationStatus;
import com.oceanview.resort.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller for Reservation operations.
 * Provides CRUD endpoints for managing reservations.
 */
@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = "*")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    /**
     * POST /api/reservations
     * Create a new reservation.
     */
    @PostMapping
    public ResponseEntity<?> createReservation(@Valid @RequestBody ReservationRequest request) {
        try {
            Reservation reservation = reservationService.createReservation(request);
            return ResponseEntity.ok(mapReservation(reservation));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/reservations
     * Get all reservations.
     */
    @GetMapping
    public ResponseEntity<?> getAllReservations() {
        List<Reservation> reservations = reservationService.getAllReservations();
        List<Map<String, Object>> result = reservations.stream()
                .map(this::mapReservation)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/reservations/{id}
     * Get a specific reservation by its reservation ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getReservation(@PathVariable("id") String reservationId) {
        try {
            Reservation reservation = reservationService.findByReservationId(reservationId);
            return ResponseEntity.ok(mapReservation(reservation));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/reservations/search?keyword=...
     * Search reservations by keyword.
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchReservations(@RequestParam String keyword) {
        List<Reservation> results = reservationService.searchReservations(keyword);
        List<Map<String, Object>> mapped = results.stream()
                .map(this::mapReservation)
                .collect(Collectors.toList());
        return ResponseEntity.ok(mapped);
    }

    /**
     * PUT /api/reservations/{id}/cancel
     * Cancel a reservation.
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelReservation(@PathVariable("id") String reservationId) {
        try {
            Reservation reservation = reservationService.cancelReservation(reservationId);
            return ResponseEntity.ok(mapReservation(reservation));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/reservations/{id}/status
     * Update reservation status.
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable("id") String reservationId,
            @RequestBody Map<String, String> body) {
        try {
            ReservationStatus newStatus = ReservationStatus.valueOf(body.get("status"));
            Reservation reservation = reservationService.updateStatus(reservationId, newStatus);
            return ResponseEntity.ok(mapReservation(reservation));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Map Reservation entity to a response map (avoids circular references in
     * JSON).
     */
    private Map<String, Object> mapReservation(Reservation reservation) {
        Map<String, Object> map = new HashMap<>();
        map.put("reservationId", reservation.getReservationId());
        map.put("guestName", reservation.getGuest().getName());
        map.put("guestAddress", reservation.getGuest().getAddress());
        map.put("contactNumber", reservation.getGuest().getContactNumber());
        map.put("email", reservation.getGuest().getEmail());
        map.put("roomNumber", reservation.getRoom().getRoomNumber());
        map.put("roomType", reservation.getRoom().getRoomType().name());
        map.put("viewType", reservation.getRoom().getViewType().name());
        map.put("pricePerNight", reservation.getRoom().getPricePerNight());
        map.put("checkInDate", reservation.getCheckInDate().toString());
        map.put("checkOutDate", reservation.getCheckOutDate().toString());
        map.put("status", reservation.getStatus().name());
        map.put("totalAmount", reservation.getTotalAmount());
        map.put("nights", reservation.calculateNights());
        return map;
    }
}
