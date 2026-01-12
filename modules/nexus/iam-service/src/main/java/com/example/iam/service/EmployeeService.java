package com.example.iam.service;

import com.example.iam.dto.UpdateEmployeeRequestDto;
import com.example.iam.entity.EmployeeEntity;
import com.example.iam.entity.UserEntity;
import com.example.iam.repository.EmployeeRepository;
import com.example.iam.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    public EmployeeEntity getEmployeeByUserId(UUID userId) {
        return employeeRepository.findByUserUserId(userId)
                .orElseThrow(() -> new RuntimeException("Employee profile not found for user: " + userId));
    }

    @Transactional
    public EmployeeEntity createEmployeeProfile(@org.springframework.lang.NonNull UUID userId,
            UpdateEmployeeRequestDto request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        if (employeeRepository.findByUserUserId(userId).isPresent()) {
            throw new RuntimeException("Employee profile already exists for user: " + userId);
        }

        EmployeeEntity employee = new EmployeeEntity();
        employee.setUser(user);
        employee.setDepartment(request.getDepartment());
        employee.setJobTitle(request.getJobTitle());
        employee.setLegacyId(request.getLegacyId());
        employee.setAttributes(request.getAttributes());

        return employeeRepository.save(employee);
    }

    @Transactional
    @SuppressWarnings("null")
    public EmployeeEntity updateEmployeeProfile(UUID userId, UpdateEmployeeRequestDto request) {
        EmployeeEntity employee = getEmployeeByUserId(userId);

        if (request.getDepartment() != null) {
            employee.setDepartment(request.getDepartment());
        }
        if (request.getJobTitle() != null) {
            employee.setJobTitle(request.getJobTitle());
        }
        if (request.getLegacyId() != null) {
            employee.setLegacyId(request.getLegacyId());
        }
        if (request.getAttributes() != null) {
            employee.setAttributes(request.getAttributes());
        }

        return employeeRepository.save(employee);
    }
}
