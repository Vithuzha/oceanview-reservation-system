package com.oceanview.resort.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT Utility class for generating and validating JSON Web Tokens.
 * Used for stateless authentication in the REST API.
 */
@Component
public class JwtUtil {

    private final SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final long EXPIRATION_TIME = 86400000; // 24 hours in milliseconds

    /**
     * Generate a JWT token for the authenticated user.
     */
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key)
                .compact();
    }

    /**
     * Extract username from a JWT token.
     */
    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Extract user role from a JWT token.
     */
    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    /**
     * Validate a JWT token.
     */
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
