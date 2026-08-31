package com.optimizely.scheduler.service;

import com.optimizely.scheduler.dto.AuthRequest;
import com.optimizely.scheduler.dto.AuthResponse;
import com.optimizely.scheduler.dto.UserResponse;
import com.optimizely.scheduler.entity.User;
import com.optimizely.scheduler.repository.UserRepository;
import com.optimizely.scheduler.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Handles user registration and login.
 * Registration creates a new account; login verifies credentials
 * and returns a signed JWT.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * Register a new user. Throws if the email is already taken.
     *
     * @param request registration payload with email and password
     * @return the public representation of the created user
     */
    public UserResponse register(AuthRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        return UserResponse.from(userRepository.save(user));
    }

    /**
     * Authenticate an existing user and return a JWT.
     *
     * @param request login payload with email and password
     * @return the token and its expiration window
     * @throws IllegalArgumentException if credentials are invalid
     */
    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(token, jwtUtil.getExpirationSeconds());
    }
}