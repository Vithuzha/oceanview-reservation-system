package com.oceanview.resort.repository;

import com.oceanview.resort.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Spring Data JPA Repository for Bill entity.
 */
@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    Optional<Bill> findByReservationReservationId(String reservationId);

    @Query("SELECT SUM(b.totalAmount) FROM Bill b")
    Double getTotalRevenue();

    @Query("SELECT SUM(b.taxAmount) FROM Bill b")
    Double getTotalTaxCollected();
}
