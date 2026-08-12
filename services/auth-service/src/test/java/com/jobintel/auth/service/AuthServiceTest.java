package com.jobintel.auth.service;

import com.jobintel.auth.domain.User;
import com.jobintel.auth.dto.AuthResponse;
import com.jobintel.auth.dto.LoginRequest;
import com.jobintel.auth.dto.RegisterRequest;
import com.jobintel.auth.repository.UserRepository;
import com.jobintel.common.exception.DuplicateResourceException;
import com.jobintel.common.exception.ValidationException;
import com.jobintel.common.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(UUID.randomUUID())
                .email("user@example.com")
                .passwordHash("encoded_pass")
                .fullName("Sample User")
                .role("USER")
                .enabled(true)
                .build();
    }

    @Test
    void register_success() {
        RegisterRequest request = new RegisterRequest("user@example.com", "Password123!", "Sample User");

        when(userRepository.existsByEmailIgnoreCase("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("encoded_pass");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(jwtTokenProvider.createAccessToken(any(), any(), any())).thenReturn("access_token");
        when(jwtTokenProvider.createRefreshToken(any())).thenReturn("refresh_token");

        AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo("user@example.com");
        assertThat(response.accessToken()).isEqualTo("access_token");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicateEmail_throwsException() {
        RegisterRequest request = new RegisterRequest("user@example.com", "Password123!", "Sample User");

        when(userRepository.existsByEmailIgnoreCase("user@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email already registered");
    }

    @Test
    void login_success() {
        LoginRequest request = new LoginRequest("user@example.com", "Password123!");

        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("Password123!", "encoded_pass")).thenReturn(true);
        when(jwtTokenProvider.createAccessToken(any(), any(), any())).thenReturn("access_token");
        when(jwtTokenProvider.createRefreshToken(any())).thenReturn("refresh_token");

        AuthResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo("user@example.com");
    }

    @Test
    void login_invalidPassword_throwsValidationException() {
        LoginRequest request = new LoginRequest("user@example.com", "WrongPassword");

        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("WrongPassword", "encoded_pass")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid credentials");
    }
}
