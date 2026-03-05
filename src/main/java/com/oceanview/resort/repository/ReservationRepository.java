package com.oceanview.resort.repository;

import com.oceanview.resort.model.Reservation;
import com.oceanview.resort.model.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for Reservation entity.
 */
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findByReservationId(String reservationId);

    List<Reservation> findByStatus(ReservationStatus status);

    @Query("SELECT r FROM Reservation r WHERE r.guest.name LIKE %:keyword% " +
            "OR r.guest.contactNumber LIKE %:keyword% " +
            "OR r.reservationId LIKE %:keyword%")
    List<Reservation> searchReservations(@Param("keyword") String keyword);

    @Query("SELECT r FROM Reservation r WHERE r.room.roomId = :roomId " +
            "AND r.status NOT IN ('CANCELLED', 'CHECKED_OUT') " +
            "AND r.checkInDate < :checkOut AND r.checkOutDate > :checkIn")
    List<Reservation> findConflictingReservations(@Param("roomId") Long roomId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.status = :status")
    long countByStatus(@Param("status") ReservationStatus status);

    @Query("SELECT r FROM Reservation r WHERE r.checkInDate BETWEEN :startDate AND :endDate")
    List<Reservation> findByCheckInDateBetween(@Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    List<Reservation> findAllByOrderByCheckInDateDesc();
}
