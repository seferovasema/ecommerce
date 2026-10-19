package com.sema.ecommerce.service;

import com.sema.ecommerce.dto.request.LoginRequest;
import com.sema.ecommerce.dto.response.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
}
