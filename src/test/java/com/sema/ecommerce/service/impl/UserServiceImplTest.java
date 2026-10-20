package com.sema.ecommerce.service.impl;

import com.sema.ecommerce.dto.request.UserRequest;
import com.sema.ecommerce.dto.response.UserResponse;
import com.sema.ecommerce.entity.User;
import com.sema.ecommerce.enums.Role;
import com.sema.ecommerce.exception.EmailAlreadyExistsException;
import com.sema.ecommerce.exception.ResourceNotFoundException;
import com.sema.ecommerce.mapper.UserMapper;
import com.sema.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUser_shouldCreateSuccessfully() {
        UserRequest request = mock(UserRequest.class);
        when(request.getEmail()).thenReturn("test@example.com");
        when(request.getPassword()).thenReturn("password123");

        User user = mock(User.class);
        UserResponse response = mock(UserResponse.class);

        when(userRepository.existsByEmail("test@example.com"))
                .thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(user);
        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(response);

        UserResponse result = userService.createUser(request);

        assertSame(response, result);
        verify(user).setPassword("encodedPassword");
        verify(user).setRole(Role.USER);
        verify(userRepository).save(user);
    }

    @Test
    void createUser_shouldThrowExceptionWhenEmailAlreadyExists() {
        UserRequest request = mock(UserRequest.class);
        when(request.getEmail()).thenReturn("test@example.com");

        when(userRepository.existsByEmail("test@example.com"))
                .thenReturn(true);

        assertThrows(
                EmailAlreadyExistsException.class,
                () -> userService.createUser(request)
        );

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void getUserById_shouldReturnUserSuccessfully() {
        Long id = 1L;

        User user = mock(User.class);
        UserResponse response = mock(UserResponse.class);

        when(userRepository.findById(id))
                .thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(response);

        UserResponse result = userService.getUserById(id);

        assertSame(response, result);
    }

    @Test
    void getUserById_shouldThrowExceptionWhenNotFound() {
        Long id = 1L;

        when(userRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getUserById(id)
        );
    }

    @Test
    void updateUser_shouldUpdateSuccessfully() {
        Long id = 1L;

        UserRequest request = mock(UserRequest.class);
        when(request.getEmail()).thenReturn("new@example.com");

        User user = mock(User.class);
        when(user.getEmail()).thenReturn("old@example.com");

        UserResponse response = mock(UserResponse.class);

        when(userRepository.findById(id))
                .thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("new@example.com"))
                .thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(response);

        UserResponse result = userService.updateUser(id, request);

        assertSame(response, result);
        verify(userMapper).updateUser(request, user);
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_shouldThrowExceptionWhenUserNotFound() {
        Long id = 1L;
        UserRequest request = mock(UserRequest.class);

        when(userRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.updateUser(id, request)
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_shouldThrowExceptionWhenEmailAlreadyExists() {
        Long id = 1L;

        UserRequest request = mock(UserRequest.class);
        when(request.getEmail()).thenReturn("existing@example.com");

        User user = mock(User.class);
        when(user.getEmail()).thenReturn("old@example.com");

        when(userRepository.findById(id))
                .thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("existing@example.com"))
                .thenReturn(true);

        assertThrows(
                EmailAlreadyExistsException.class,
                () -> userService.updateUser(id, request)
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_shouldDeleteSuccessfully() {
        Long id = 1L;
        User user = mock(User.class);

        when(userRepository.findById(id))
                .thenReturn(Optional.of(user));

        userService.deleteUser(id);

        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_shouldThrowExceptionWhenNotFound() {
        Long id = 1L;

        when(userRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.deleteUser(id)
        );

        verify(userRepository, never()).delete(any(User.class));
    }
}
