package com.sema.ecommerce.service.impl;

import com.sema.ecommerce.dto.request.LoginRequest;
import com.sema.ecommerce.dto.response.LoginResponse;
import com.sema.ecommerce.entity.User;
import com.sema.ecommerce.exception.InvalidCredentialsException;
import com.sema.ecommerce.repository.UserRepository;
import com.sema.ecommerce.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void login_shouldLoginSuccessfully() {
        String email = "test@example.com";
        String password = "password123";
        String encodedPassword = "encodedPassword";
        String token = "jwt-token";

        LoginRequest request = mock(LoginRequest.class);
        when(request.getEmail()).thenReturn(email);
        when(request.getPassword()).thenReturn(password);

        User user = mock(User.class);
        when(user.getEmail()).thenReturn(email);
        when(user.getPassword()).thenReturn(encodedPassword);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, encodedPassword))
                .thenReturn(true);
        when(jwtService.generateToken(email)).thenReturn(token);

        LoginResponse result = authService.login(request);

        assertNotNull(result);
        assertEquals(token, result.getToken());

        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(password, encodedPassword);
        verify(jwtService).generateToken(email);
    }

    @Test
    void login_shouldThrowExceptionWhenUserNotFound() {
        String email = "missing@example.com";

        LoginRequest request = mock(LoginRequest.class);
        when(request.getEmail()).thenReturn(email);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );

        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    void login_shouldThrowExceptionWhenPasswordIsIncorrect() {
        String email = "test@example.com";
        String password = "wrongPassword";
        String encodedPassword = "encodedPassword";

        LoginRequest request = mock(LoginRequest.class);
        when(request.getEmail()).thenReturn(email);
        when(request.getPassword()).thenReturn(password);

        User user = mock(User.class);
        when(user.getPassword()).thenReturn(encodedPassword);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, encodedPassword))
                .thenReturn(false);

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );

        verify(jwtService, never()).generateToken(anyString());
    }
}
