package com.aikb.security;

import com.aikb.config.JwtConfig;
import com.aikb.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtConfig jwtConfig;
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtConfig = new JwtConfig();
        jwtConfig.setSecret("test-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm");
        jwtConfig.setAccessTtl(900000L);  // 15 minutes
        jwtConfig.setRefreshTtl(604800000L);  // 7 days
        jwtTokenProvider = new JwtTokenProvider(jwtConfig);
    }

    @Test
    void generateAccessToken_createsValidToken() {
        User user = User.builder().id(1L).email("test@example.com").build();

        String token = jwtTokenProvider.generateAccessToken(user);

        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3);  // JWT has 3 parts
    }

    @Test
    void generateRefreshToken_createsValidToken() {
        User user = User.builder().id(1L).email("test@example.com").build();

        String token = jwtTokenProvider.generateRefreshToken(user);

        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    void validateToken_withValidToken_returnsTrue() {
        User user = User.builder().id(1L).email("test@example.com").build();
        String token = jwtTokenProvider.generateAccessToken(user);

        boolean isValid = jwtTokenProvider.validateToken(token);

        assertTrue(isValid);
    }

    @Test
    void validateToken_withInvalidToken_returnsFalse() {
        boolean isValid = jwtTokenProvider.validateToken("invalid.token.here");

        assertFalse(isValid);
    }

    @Test
    void getUsernameFromToken_returnsCorrectEmail() {
        User user = User.builder().id(1L).email("test@example.com").build();
        String token = jwtTokenProvider.generateAccessToken(user);

        String extractedEmail = jwtTokenProvider.getUsernameFromToken(token);

        assertEquals("test@example.com", extractedEmail);
    }

    @Test
    void getClaimFromToken_returnsCorrectClaim() {
        User user = User.builder().id(1L).email("test@example.com").build();
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        String accessType = jwtTokenProvider.getClaimFromToken(accessToken, "type");
        String refreshType = jwtTokenProvider.getClaimFromToken(refreshToken, "type");

        assertEquals("access", accessType);
        assertEquals("refresh", refreshType);
    }

    @Test
    void getRemainingTime_returnsPositiveValue() {
        User user = User.builder().id(1L).email("test@example.com").build();
        String token = jwtTokenProvider.generateAccessToken(user);

        long remainingTime = jwtTokenProvider.getRemainingTime(token);

        assertTrue(remainingTime > 0);
        assertTrue(remainingTime <= 900000L);  // Should be <= 15 minutes
    }
}
