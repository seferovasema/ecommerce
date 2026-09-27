package com.sema.ecommerce.service.impl;

import com.sema.ecommerce.dto.request.UserRequest;
import com.sema.ecommerce.dto.response.UserResponse;
import com.sema.ecommerce.entity.User;
import com.sema.ecommerce.enums.Role;
import com.sema.ecommerce.exception.EmailAlreadyExistsException;
import com.sema.ecommerce.exception.ResourceNotFoundException;
import com.sema.ecommerce.mapper.UserMapper;
import com.sema.ecommerce.repository.UserRepository;
import com.sema.ecommerce.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())){
            throw new EmailAlreadyExistsException( "Email already exists: " + request.getEmail());
        }
        User user = userMapper.toEntity(request);
        user.setRole(Role.USER);

        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse updateUser(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + id
                        )
                );
        if(!user.getEmail().equals(request.getEmail())&&userRepository.existsByEmail(request.getEmail())){
            throw new EmailAlreadyExistsException(
                    "Email already exists: " + request.getEmail()
            );
        }
        userMapper.updateUser(request,user);
        User updatedUser = userRepository.save(user);

        return userMapper.toResponse(updatedUser);

    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + id
                        )
                );
        userRepository.delete(user);
    }
}
