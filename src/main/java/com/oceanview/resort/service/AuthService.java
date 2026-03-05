package com.oceanview.resort.service;

import com.oceanview.resort.config.JwtUtil;
import com.oceanview.resort.dto.LoginRequest;
import com.oceanview.resort.dto.LoginResponse;
import com.oceanview.resort.model.User;
import com.oceanview.resort.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Authentication Service.
 * Handles user login with BCrypt password validation and JWT token generation.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Validates user credentials and generates a JWT token on success.
     * Error messages do not reveal whether username or password was incorrect
     * (security requirement).
     */
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!user.isActive()) {
            throw new RuntimeException("Account is deactivated");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

        return new LoginResponse(token, user.getUsername(), user.getFullName(), user.getRole().name());
    }
}
