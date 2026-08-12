package com.jobintel.auth.service;

import com.jobintel.auth.domain.User;
import com.jobintel.auth.dto.*;
import com.jobintel.auth.repository.UserRepository;
import com.jobintel.common.exception.DuplicateResourceException;
import com.jobintel.common.exception.ValidationException;
import com.jobintel.common.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.access-token-expiry-ms:900000}")
    private long accessTokenExpiryMs;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException("Email already registered");
        }
        User user = User.builder()
                .email(request.email().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .role("USER")
                .enabled(true)
                .build();
        userRepository.save(user);
        return buildAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new ValidationException("Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ValidationException("Invalid credentials");
        }
        if (!user.isEnabled()) {
            throw new ValidationException("Account disabled");
        }
        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        List<String> roles = List.of(user.getRole());
        String access = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), roles);
        String refresh = jwtTokenProvider.createRefreshToken(user.getId());
        return new AuthResponse(user.getId(), user.getEmail(), user.getFullName(), roles,
                access, refresh, accessTokenExpiryMs);
    }
}
