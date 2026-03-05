package com.oceanview.resort.repository;

import com.oceanview.resort.model.Room;
import com.oceanview.resort.model.enums.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for Room entity.
 */
@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByRoomNumber(String roomNumber);

    List<Room> findByRoomType(RoomType roomType);

    List<Room> findByIsAvailableTrue();

    @Query("SELECT r FROM Room r WHERE r.roomId NOT IN " +
            "(SELECT res.room.roomId FROM Reservation res " +
            "WHERE res.status NOT IN ('CANCELLED', 'CHECKED_OUT') " +
            "AND res.checkInDate < :checkOut AND res.checkOutDate > :checkIn)")
    List<Room> findAvailableRooms(@Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);

    @Query("SELECT r FROM Room r WHERE r.roomType = :roomType AND r.roomId NOT IN " +
            "(SELECT res.room.roomId FROM Reservation res " +
            "WHERE res.status NOT IN ('CANCELLED', 'CHECKED_OUT') " +
            "AND res.checkInDate < :checkOut AND res.checkOutDate > :checkIn)")
    List<Room> findAvailableRoomsByType(@Param("roomType") RoomType roomType,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);
}
