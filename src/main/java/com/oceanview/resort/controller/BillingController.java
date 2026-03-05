package com.oceanview.resort.controller;

import com.oceanview.resort.dto.BillResponse;
import com.oceanview.resort.service.BillingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for Billing operations.
 */
@RestController
@RequestMapping("/api/billing")
@CrossOrigin(origins = "*")
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    /**
     * POST /api/billing/{reservationId}
     * Generate a bill for a reservation.
     */
    @PostMapping("/{reservationId}")
    public ResponseEntity<?> generateBill(@PathVariable String reservationId) {
        try {
            BillResponse bill = billingService.generateBill(reservationId);
            return ResponseEntity.ok(bill);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/billing/{reservationId}
     * Get the bill for a reservation.
     */
    @GetMapping("/{reservationId}")
    public ResponseEntity<?> getBill(@PathVariable String reservationId) {
        try {
            BillResponse bill = billingService.getBill(reservationId);
            return ResponseEntity.ok(bill);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }
}
