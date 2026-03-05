package com.oceanview.resort.service;

import com.oceanview.resort.factory.RoomFactory;
import com.oceanview.resort.model.Room;
import com.oceanview.resort.model.enums.RoomType;
import com.oceanview.resort.model.enums.ViewType;
import com.oceanview.resort.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Service layer for Room management.
 * Uses RoomFactory for creating room instances (Factory Pattern).
 */
@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomFactory roomFactory;

    public RoomService(RoomRepository roomRepository, RoomFactory roomFactory) {
        this.roomRepository = roomRepository;
        this.roomFactory = roomFactory;
    }

    /**
     * Create a new room using the Factory Pattern.
     */
    public Room createRoom(String roomNumber, RoomType roomType, ViewType viewType) {
        Room room = roomFactory.createRoom(roomNumber, roomType, viewType);
        return roomRepository.save(room);
    }

    /**
     * Get all rooms.
     */
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    /**
     * Get available rooms for a specific date range.
     */
    public List<Room> getAvailableRooms(LocalDate checkIn, LocalDate checkOut) {
        return roomRepository.findAvailableRooms(checkIn, checkOut);
    }

    /**
     * Get available rooms by type for a specific date range.
     */
    public List<Room> getAvailableRoomsByType(RoomType roomType, LocalDate checkIn, LocalDate checkOut) {
        return roomRepository.findAvailableRoomsByType(roomType, checkIn, checkOut);
    }

    /**
     * Find a room by its room number.
     */
    public Room findByRoomNumber(String roomNumber) {
        return roomRepository.findByRoomNumber(roomNumber)
                .orElseThrow(() -> new RuntimeException("Room not found: " + roomNumber));
    }
}
