package com.oceanview.resort.dao;

import com.oceanview.resort.model.Reservation;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object (DAO) interface for Reservation entity.
 * Defines the contract for all reservation data access operations,
 * allowing loose coupling between the service and persistence layers.
 * 
 * This follows the DAO Design Pattern - separating data access logic
 * from business logic for better maintainability and testability.
 */
public interface ReservationDAO {

    Reservation save(Reservation reservation);

    Optional<Reservation> findByReservationId(String reservationId);

    List<Reservation> findAll();

    List<Reservation> searchByKeyword(String keyword);

    void deleteByReservationId(String reservationId);

    boolean existsByReservationId(String reservationId);
}
