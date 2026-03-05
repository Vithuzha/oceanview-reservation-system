package com.oceanview.resort.controller;

import com.oceanview.resort.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for Reports.
 * Provides occupancy and revenue reporting endpoints.
 */
@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * GET /api/reports/occupancy
     * Get occupancy statistics report.
     */
    @GetMapping("/occupancy")
    public ResponseEntity<Map<String, Object>> getOccupancyReport() {
        return ResponseEntity.ok(reportService.getOccupancyReport());
    }

    /**
     * GET /api/reports/revenue
     * Get revenue statistics report.
     */
    @GetMapping("/revenue")
    public ResponseEntity<Map<String, Object>> getRevenueReport() {
        return ResponseEntity.ok(reportService.getRevenueReport());
    }
}
