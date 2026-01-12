package com.example.admin_panel_service.service;

import com.example.admin_panel_service.client.IamServiceClient;
import com.example.admin_panel_service.dto.CreateUserRequestDto;
import com.example.admin_panel_service.dto.UpdateUserRequestDto;
import com.example.admin_panel_service.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final IamServiceClient iamServiceClient;

    public List<UserResponseDto> getAllUsers() {
        return iamServiceClient.getAllUsers();
    }

    public UserResponseDto getUserById(UUID userId) {
        return iamServiceClient.getUserById(userId);
    }

    public UserResponseDto createUser(CreateUserRequestDto request) {
        return iamServiceClient.createUser(request);
    }

    public UserResponseDto updateUser(UUID userId, UpdateUserRequestDto request) {
        return iamServiceClient.updateUser(userId, request);
    }

    public void deleteUser(UUID userId) {
        iamServiceClient.deleteUser(userId);
    }
}