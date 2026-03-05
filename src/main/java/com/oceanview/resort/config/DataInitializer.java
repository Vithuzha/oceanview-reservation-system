package com.oceanview.resort.config;

import com.oceanview.resort.factory.RoomFactory;
import com.oceanview.resort.model.Room;
import com.oceanview.resort.model.User;
import com.oceanview.resort.model.enums.RoomType;
import com.oceanview.resort.model.enums.UserRole;
import com.oceanview.resort.model.enums.ViewType;
import com.oceanview.resort.repository.RoomRepository;
import com.oceanview.resort.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Data Initializer - Seeds the database with default users and rooms on
 * startup.
 * Creates sample data for development and testing purposes.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoomFactory roomFactory;

    public DataInitializer(UserRepository userRepository, RoomRepository roomRepository,
            PasswordEncoder passwordEncoder, RoomFactory roomFactory) {
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.passwordEncoder = passwordEncoder;
        this.roomFactory = roomFactory;
    }

    @Override
    public void run(String... args) {
        // Create default users if they don't exist
        if (!userRepository.existsByUsername("staff")) {
            User staff = new User("staff", passwordEncoder.encode("password123"),
                    "Reception Staff", UserRole.STAFF);
            userRepository.save(staff);
        }

        if (!userRepository.existsByUsername("manager")) {
            User manager = new User("manager", passwordEncoder.encode("admin123"),
                    "Hotel Manager", UserRole.MANAGER);
            userRepository.save(manager);
        }

        // Create rooms if none exist
        if (roomRepository.count() == 0) {
            // Standard Rooms (101-106)
            saveRoom("101", RoomType.STANDARD, ViewType.GARDEN);
            saveRoom("102", RoomType.STANDARD, ViewType.GARDEN);
            saveRoom("103", RoomType.STANDARD, ViewType.POOL);
            saveRoom("104", RoomType.STANDARD, ViewType.POOL);
            saveRoom("105", RoomType.STANDARD, ViewType.OCEAN);
            saveRoom("106", RoomType.STANDARD, ViewType.OCEAN);

            // Deluxe Rooms (201-206)
            saveRoom("201", RoomType.DELUXE, ViewType.GARDEN);
            saveRoom("202", RoomType.DELUXE, ViewType.POOL);
            saveRoom("203", RoomType.DELUXE, ViewType.POOL);
            saveRoom("204", RoomType.DELUXE, ViewType.OCEAN);
            saveRoom("205", RoomType.DELUXE, ViewType.OCEAN);
            saveRoom("206", RoomType.DELUXE, ViewType.OCEAN);

            // Suite Rooms (301-304)
            saveRoom("301", RoomType.SUITE, ViewType.POOL);
            saveRoom("302", RoomType.SUITE, ViewType.OCEAN);
            saveRoom("303", RoomType.SUITE, ViewType.OCEAN);
            saveRoom("304", RoomType.SUITE, ViewType.OCEAN);
        }
    }

    private void saveRoom(String roomNumber, RoomType roomType, ViewType viewType) {
        Room room = roomFactory.createRoom(roomNumber, roomType, viewType);
        roomRepository.save(room);
    }
}
