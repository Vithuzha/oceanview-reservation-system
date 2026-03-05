package com.oceanview.resort.dao;

import com.oceanview.resort.model.Reservation;
import com.oceanview.resort.repository.ReservationRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

/**
 * Concrete implementation of the ReservationDAO interface.
 * This class implements the DAO Design Pattern by encapsulating
 * all database-related code for reservation operations.
 * 
 * It delegates to the Spring Data JPA ReservationRepository
 * internally while providing a clean abstraction layer.
 */
@Component
public class ReservationDAOImpl implements ReservationDAO {

    private final ReservationRepository reservationRepository;

    public ReservationDAOImpl(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public Reservation save(Reservation reservation) {
        return reservationRepository.save(reservation);
    }

    @Override
    public Optional<Reservation> findByReservationId(String reservationId) {
        return reservationRepository.findByReservationId(reservationId);
    }

    @Override
    public List<Reservation> findAll() {
        return reservationRepository.findAllByOrderByCheckInDateDesc();
    }

    @Override
    public List<Reservation> searchByKeyword(String keyword) {
        return reservationRepository.searchReservations(keyword);
    }

    @Override
    public void deleteByReservationId(String reservationId) {
        reservationRepository.findByReservationId(reservationId)
                .ifPresent(reservationRepository::delete);
    }

    @Override
    public boolean existsByReservationId(String reservationId) {
        return reservationRepository.findByReservationId(reservationId).isPresent();
    }
}
