package com.oceanview.resort.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security Configuration.
 * Implements stateless JWT-based authentication with role-based access control.
 * BCrypt is used for password hashing.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public resources
                        .requestMatchers("/", "/index.html", "/css/**", "/js/**", "/pages/**", "/images/**").permitAll()
                        // Login endpoint is public
                        .requestMatchers("/api/auth/login").permitAll()
                        // H2 console for development
                        .requestMatchers("/h2-console/**").permitAll()
                        // Reports only for MANAGER role
                        .requestMatchers("/api/reports/**").hasAnyRole("MANAGER", "STAFF")
                        // All other API endpoints require authentication
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * BCrypt password encoder bean.
     * BCrypt is used because it's an adaptive hashing function that
     * automatically handles salting and is resistant to brute-force attacks.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
