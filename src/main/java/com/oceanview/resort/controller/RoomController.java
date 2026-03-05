package com.oceanview.resort.controller;

import com.oceanview.resort.model.Room;
import com.oceanview.resort.model.enums.RoomType;
import com.oceanview.resort.service.RoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller for Room operations.
 */
@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "*")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    /**
     * GET /api/rooms
     * Get all rooms.
     */
    @GetMapping
    public ResponseEntity<?> getAllRooms() {
        List<Room> rooms = roomService.getAllRooms();
        List<Map<String, Object>> result = rooms.stream()
                .map(this::mapRoom)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/rooms/available?checkIn=...&checkOut=...&roomType=...
     * Get available rooms for a date range, optionally filtered by type.
     */
    @GetMapping("/available")
    public ResponseEntity<?> getAvailableRooms(
            @RequestParam String checkIn,
            @RequestParam String checkOut,
            @RequestParam(required = false) String roomType) {
        try {
            LocalDate checkInDate = LocalDate.parse(checkIn);
            LocalDate checkOutDate = LocalDate.parse(checkOut);

            List<Room> rooms;
            if (roomType != null && !roomType.isEmpty()) {
                rooms = roomService.getAvailableRoomsByType(
                        RoomType.valueOf(roomType), checkInDate, checkOutDate);
            } else {
                rooms = roomService.getAvailableRooms(checkInDate, checkOutDate);
            }

            List<Map<String, Object>> result = rooms.stream()
                    .map(this::mapRoom)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private Map<String, Object> mapRoom(Room room) {
        Map<String, Object> map = new HashMap<>();
        map.put("roomId", room.getRoomId());
        map.put("roomNumber", room.getRoomNumber());
        map.put("roomType", room.getRoomType().name());
        map.put("viewType", room.getViewType().name());
        map.put("pricePerNight", room.getPricePerNight());
        map.put("isAvailable", room.isAvailable());
        map.put("description", room.getDescription());
        return map;
    }
}
