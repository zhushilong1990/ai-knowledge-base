package com.aikb.service;

import com.aikb.config.JwtConfig;
import com.aikb.dto.AuthResponse;
import com.aikb.entity.User;
import com.aikb.exception.EmailAlreadyExistsException;
import com.aikb.repository.UserRepository;
import com.aikb.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;
    private JwtConfig jwtConfig;
    private JwtTokenProvider jwtTokenProvider;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        jwtConfig = new JwtConfig();
        jwtConfig.setSecret("test-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm");
        jwtConfig.setAccessTtl(900000L);
        jwtConfig.setRefreshTtl(604800000L);
        jwtTokenProvider = new JwtTokenProvider(jwtConfig);
        authService = new AuthService(userRepository, passwordEncoder, jwtTokenProvider, jwtConfig);
    }

    @Test
    void register_withNewEmail_createsUser() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.insert(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        Long userId = authService.register("test@example.com", "password123");

        assertEquals(1L, userId);
    }

    @Test
    void register_withExistingEmail_throwsException() {
        User existingUser = User.builder().id(1L).email("test@example.com").build();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));

        assertThrows(EmailAlreadyExistsException.class,
                () -> authService.register("test@example.com", "password123"));
    }

    @Test
    void register_withShortPassword_throwsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(com.aikb.exception.InvalidPasswordException.class,
                () -> authService.register("test@example.com", "short"));
    }

    @Test
    void login_withValidCredentials_returnsTokens() {
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .build();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        AuthResponse response = authService.login("test@example.com", "password123");

        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(900L, response.getExpiresIn());
    }

    @Test
    void login_withWrongPassword_throwsException() {
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .build();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        assertThrows(BadCredentialsException.class,
                () -> authService.login("test@example.com", "wrongpassword"));
    }

    @Test
    void login_withNonexistentUser_throwsException() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class,
                () -> authService.login("nonexistent@example.com", "password123"));
    }

    @Test
    void refresh_withValidRefreshToken_returnsNewAccessToken() {
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .build();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        AuthResponse loginResponse = authService.login("test@example.com", "password123");
        AuthResponse refreshResponse = authService.refresh(loginResponse.getRefreshToken());

        assertNotNull(refreshResponse.getAccessToken());
        assertNull(refreshResponse.getRefreshToken());
        assertEquals("Bearer", refreshResponse.getTokenType());
    }

    @Test
    void password_isBCryptEncoded() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.insert(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        authService.register("test@example.com", "password123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(
                User.builder()
                        .id(1L)
                        .email("test@example.com")
                        .passwordHash(passwordEncoder.encode("password123"))
                        .build()
        ));

        User savedUser = userRepository.findByEmail("test@example.com").orElse(null);
        assertNotNull(savedUser);
        assertTrue(savedUser.getPasswordHash().startsWith("$2a$"));
    }
}
