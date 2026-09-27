package com.sema.ecommerce.service;

import com.sema.ecommerce.dto.request.UserRequest;
import com.sema.ecommerce.dto.response.UserResponse;

public interface UserService {

    UserResponse createUser(UserRequest request);

    UserResponse getUserById(Long id);

    UserResponse updateUser(Long id,UserRequest request);
    void deleteUser(Long id);
}
