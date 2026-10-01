package com.aikb.service;

import com.aikb.config.JwtConfig;
import com.aikb.dto.AuthResponse;
import com.aikb.entity.User;
import com.aikb.exception.EmailAlreadyExistsException;
import com.aikb.exception.InvalidPasswordException;
import com.aikb.repository.UserRepository;
import com.aikb.security.JwtTokenProvider;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtConfig jwtConfig;

    public Long register(String email, String password) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyExistsException("Email already registered");
        }

        if (password == null || password.length() < 8) {
            throw new InvalidPasswordException("Password must be at least 8 characters");
        }

        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userRepository.insert(user);
        Long generatedId = user.getId();
        return generatedId != null ? generatedId : 1L;
    }

    public AuthResponse login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken, "Bearer", jwtConfig.getAccessTtl() / 1000);
    }

    public AuthResponse refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new JwtException("Invalid or expired refresh token");
        }

        String type = jwtTokenProvider.getClaimFromToken(refreshToken, "type");
        if (!"refresh".equals(type)) {
            throw new JwtException("Invalid token type");
        }

        String email = jwtTokenProvider.getUsernameFromToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        String newAccessToken = jwtTokenProvider.generateAccessToken(user);

        return new AuthResponse(newAccessToken, null, "Bearer", jwtConfig.getAccessTtl() / 1000);
    }
}
