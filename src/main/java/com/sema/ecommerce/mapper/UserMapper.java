package com.sema.ecommerce.mapper;

import com.sema.ecommerce.dto.request.UserRequest;
import com.sema.ecommerce.dto.response.UserResponse;
import com.sema.ecommerce.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toEntity(UserRequest request);
    UserResponse toResponse(User user);
    void updateUser(UserRequest request, @MappingTarget User user);
}
