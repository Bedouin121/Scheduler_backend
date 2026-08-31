package com.optimizely.scheduler.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Returned after a successful login, contains the JWT and when it expires.
 */
@Getter
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private long expiresIn;
}