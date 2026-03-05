package com.oceanview.resort.service;

import com.oceanview.resort.model.enums.ReservationStatus;
import com.oceanview.resort.repository.BillRepository;
import com.oceanview.resort.repository.ReservationRepository;
import com.oceanview.resort.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service layer for generating reports.
 * Provides occupancy statistics, revenue summaries, and reservation analytics.
 */
@Service
public class ReportService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final BillRepository billRepository;

    public ReportService(ReservationRepository reservationRepository,
            RoomRepository roomRepository,
            BillRepository billRepository) {
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
        this.billRepository = billRepository;
    }

    /**
     * Generate occupancy report with room and reservation statistics.
     */
    public Map<String, Object> getOccupancyReport() {
        Map<String, Object> report = new HashMap<>();

        long totalRooms = roomRepository.count();
        long confirmedReservations = reservationRepository.countByStatus(ReservationStatus.CONFIRMED);
        long checkedInReservations = reservationRepository.countByStatus(ReservationStatus.CHECKED_IN);
        long pendingReservations = reservationRepository.countByStatus(ReservationStatus.PENDING);
        long cancelledReservations = reservationRepository.countByStatus(ReservationStatus.CANCELLED);
        long checkedOutReservations = reservationRepository.countByStatus(ReservationStatus.CHECKED_OUT);
        long totalReservations = reservationRepository.count();

        long occupiedRooms = confirmedReservations + checkedInReservations;
        double occupancyRate = totalRooms > 0 ? (double) occupiedRooms / totalRooms * 100 : 0;

        report.put("totalRooms", totalRooms);
        report.put("occupiedRooms", occupiedRooms);
        report.put("availableRooms", totalRooms - occupiedRooms);
        report.put("occupancyRate", Math.round(occupancyRate * 100.0) / 100.0);
        report.put("totalReservations", totalReservations);
        report.put("confirmedReservations", confirmedReservations);
        report.put("checkedInReservations", checkedInReservations);
        report.put("pendingReservations", pendingReservations);
        report.put("cancelledReservations", cancelledReservations);
        report.put("checkedOutReservations", checkedOutReservations);

        return report;
    }

    /**
     * Generate revenue report with billing statistics.
     */
    public Map<String, Object> getRevenueReport() {
        Map<String, Object> report = new HashMap<>();

        Double totalRevenue = billRepository.getTotalRevenue();
        Double totalTax = billRepository.getTotalTaxCollected();
        long totalBills = billRepository.count();

        report.put("totalRevenue", totalRevenue != null ? totalRevenue : 0.0);
        report.put("totalTaxCollected", totalTax != null ? totalTax : 0.0);
        report.put("totalBillsGenerated", totalBills);
        report.put("averageBillAmount",
                totalBills > 0 ? (totalRevenue != null ? totalRevenue / totalBills : 0.0) : 0.0);

        return report;
    }
}
