package com.oceanview.resort.factory;

import com.oceanview.resort.model.Room;
import com.oceanview.resort.model.enums.RoomType;
import com.oceanview.resort.model.enums.ViewType;
import org.springframework.stereotype.Component;

/**
 * Factory Pattern implementation for creating Room objects.
 * Instead of directly instantiating Room objects, this factory
 * encapsulates the creation logic and automatically sets the
 * price based on room type.
 * 
 * Room Prices (LKR per night):
 * - Standard: 8,000
 * - Deluxe: 14,000
 * - Suite: 25,000
 * 
 * This follows the Open/Closed Principle — new room types can be
 * added by extending this factory without modifying existing code.
 */
@Component
public class RoomFactory {

    public static final double STANDARD_PRICE = 8000.0;
    public static final double DELUXE_PRICE = 14000.0;
    public static final double SUITE_PRICE = 25000.0;

    /**
     * Creates a Room object with the correct price based on room type.
     *
     * @param roomNumber The room number (e.g., "101", "201")
     * @param roomType   The type of room (STANDARD, DELUXE, SUITE)
     * @param viewType   The view type (GARDEN, POOL, OCEAN)
     * @return A fully configured Room object
     */
    public Room createRoom(String roomNumber, RoomType roomType, ViewType viewType) {
        double price = getPriceForType(roomType);
        Room room = new Room(roomNumber, roomType, viewType, price);
        room.setDescription(generateDescription(roomType, viewType));
        return room;
    }

    /**
     * Returns the nightly rate for a given room type.
     */
    public double getPriceForType(RoomType roomType) {
        return switch (roomType) {
            case STANDARD -> STANDARD_PRICE;
            case DELUXE -> DELUXE_PRICE;
            case SUITE -> SUITE_PRICE;
        };
    }

    /**
     * Generates a descriptive text for the room.
     */
    private String generateDescription(RoomType roomType, ViewType viewType) {
        return String.format("%s room with %s view at Ocean View Resort",
                roomType.name().charAt(0) + roomType.name().substring(1).toLowerCase(),
                viewType.name().charAt(0) + viewType.name().substring(1).toLowerCase());
    }
}
