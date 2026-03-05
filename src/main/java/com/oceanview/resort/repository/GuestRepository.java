package com.oceanview.resort.repository;

import com.oceanview.resort.model.Guest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Spring Data JPA Repository for Guest entity.
 */
@Repository
public interface GuestRepository extends JpaRepository<Guest, Long> {
    List<Guest> findByNameContainingIgnoreCase(String name);

    List<Guest> findByContactNumber(String contactNumber);
}
