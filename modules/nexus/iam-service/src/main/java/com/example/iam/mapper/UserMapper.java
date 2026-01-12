package com.example.iam.mapper;

import com.example.danxils_commons.event.UserUpdatedEvent;
import com.example.iam.dto.UserDetailsDto;
import com.example.iam.dto.UserResponseDto;
import com.example.iam.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ARCHITEKTURA: Mapper ręczny (zastępujący MapStruct z powodu problemów z
 * generowaniem kodu/classloaderem).
 * Odpowiada za transformację encji użytkownika na DTO.
 */
@Component
public class UserMapper {

    public UserResponseDto toResponseDto(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        UserResponseDto dto = new UserResponseDto();
        dto.setUserId(entity.getUserId().toString());
        dto.setUsername(entity.getUsername());
        dto.setEmail(entity.getEmail());
        dto.setEnabled(entity.isEnabled());
        dto.setLegacyId(entity.getLegacyId());
        dto.setFullName(entity.getFullName());
        dto.setAvatarUrl(entity.getAvatarUrl());
        dto.setBio(entity.getBio());
        // Map roles from entity - convert UserRole enum to String
        Set<String> roleStrings = entity.getRoles() != null
                ? entity.getRoles().stream()
                        .map(Enum::name)
                        .collect(Collectors.toSet())
                : Collections.emptySet();
        dto.setRoles(roleStrings);
        return dto;
    }

    public UserDetailsDto toDetailsDto(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        UserDetailsDto dto = new UserDetailsDto();
        dto.setUserId(entity.getUserId());
        dto.setUsername(entity.getUsername());
        dto.setEmail(entity.getEmail());
        dto.setFullName(entity.getFullName());
        dto.setAvatarUrl(entity.getAvatarUrl());
        // Map other fields as needed
        return dto;
    }

    public List<UserDetailsDto> toDetailsDtoList(List<UserEntity> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(this::toDetailsDto)
                .collect(Collectors.toList());
    }

    public UserUpdatedEvent.UserPayload toUserPayload(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        Set<String> roles = entity.getRoles() != null
                ? entity.getRoles().stream()
                        .map(Enum::name)
                        .collect(Collectors.toSet())
                : Collections.emptySet();

        return UserUpdatedEvent.UserPayload.builder()
                .username(entity.getUsername())
                .email(entity.getEmail())
                .enabled(entity.isEnabled())
                .roles(roles)
                .fullName(entity.getFullName())
                .avatarUrl(entity.getAvatarUrl())
                .bio(entity.getBio())
                .build();
    }
}