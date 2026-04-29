package com.emekachukwulobe.flightbookingservice.service;

import com.emekachukwulobe.flightbookingservice.dto.request.CreateUserRequest;
import com.emekachukwulobe.flightbookingservice.dto.request.UpdatePasswordRequest;
import com.emekachukwulobe.flightbookingservice.dto.response.UserResponse;

import java.util.UUID;

public interface UserService {

    UserResponse createUser(CreateUserRequest request, UUID tenantId);

    void updatePassword(UUID userId, UpdatePasswordRequest request, UUID tenantId);

    UserResponse getUserById(UUID userId, UUID tenantId);
}
