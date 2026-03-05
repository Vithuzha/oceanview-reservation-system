package com.oceanview.resort.service;

import com.oceanview.resort.config.JwtUtil;
import com.oceanview.resort.dto.LoginRequest;
import com.oceanview.resort.dto.LoginResponse;
import com.oceanview.resort.model.User;
import com.oceanview.resort.model.enums.UserRole;
import com.oceanview.resort.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService.
 * Tests login success/failure scenarios using mock objects.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("staff", "$2a$10$hashedPassword", "Test Staff", UserRole.STAFF);
        testUser.setUserId(1L);
    }

    @Test
    @DisplayName("Login should succeed with valid credentials")
    void loginSuccess() {
        // Arrange
        LoginRequest request = new LoginRequest("staff", "password123");
        when(userRepository.findByUsername("staff")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken("staff", "STAFF")).thenReturn("mock-jwt-token");

        // Act
        LoginResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getToken());
        assertEquals("staff", response.getUsername());
        assertEquals("STAFF", response.getRole());
        assertEquals("Test Staff", response.getFullName());
        verify(userRepository).findByUsername("staff");
    }

    @Test
    @DisplayName("Login should fail with wrong username")
    void loginFailWrongUsername() {
        // Arrange
        LoginRequest request = new LoginRequest("nonexistent", "password123");
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(request));
        assertEquals("Invalid username or password", ex.getMessage());
    }

    @Test
    @DisplayName("Login should fail with wrong password")
    void loginFailWrongPassword() {
        // Arrange
        LoginRequest request = new LoginRequest("staff", "wrongpassword");
        when(userRepository.findByUsername("staff")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", testUser.getPassword())).thenReturn(false);

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(request));
        assertEquals("Invalid username or password", ex.getMessage());
    }

    @Test
    @DisplayName("Login should fail for deactivated user")
    void loginFailInactiveUser() {
        // Arrange
        testUser.setActive(false);
        LoginRequest request = new LoginRequest("staff", "password123");
        when(userRepository.findByUsername("staff")).thenReturn(Optional.of(testUser));

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(request));
        assertEquals("Account is deactivated", ex.getMessage());
    }
}
