package com.rishi.student_management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        String secret = Base64.getEncoder().encodeToString(
                "educore-unit-test-secret-key-at-least-32-bytes".getBytes()
        );
        ReflectionTestUtils.setField(jwtService, "secretKey", secret);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 60_000L);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", 300_000L);

        userDetails = User.withUsername("rishi")
                .password("encoded")
                .roles("ADMIN")
                .build();
    }

    @Test
    void accessToken_shouldBeValidForApiAndRejectedAsRefreshToken() {
        String accessToken = jwtService.generateToken(Map.of("role", "ROLE_ADMIN"), "rishi");

        assertEquals("rishi", jwtService.extractUsername(accessToken));
        assertTrue(jwtService.isTokenValid(accessToken, userDetails));
        assertFalse(jwtService.validateRefreshToken(accessToken));
    }

    @Test
    void refreshToken_shouldBeValidForRefreshAndRejectedAsAccessToken() {
        String refreshToken = jwtService.generateRefreshToken("rishi");

        assertEquals("rishi", jwtService.extractUsername(refreshToken));
        assertTrue(jwtService.validateRefreshToken(refreshToken));
        assertFalse(jwtService.isTokenValid(refreshToken, userDetails));
    }
}
