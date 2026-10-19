package com.sema.ecommerce.service.impl;

import com.sema.ecommerce.dto.request.LoginRequest;
import com.sema.ecommerce.dto.response.LoginResponse;
import com.sema.ecommerce.entity.User;
import com.sema.ecommerce.exception.InvalidCredentialsException;
import com.sema.ecommerce.repository.UserRepository;
import com.sema.ecommerce.security.JwtService;
import com.sema.ecommerce.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new InvalidCredentialsException();
        }

        return new LoginResponse(
                jwtService.generateToken(user.getEmail())
        );
    }
}
