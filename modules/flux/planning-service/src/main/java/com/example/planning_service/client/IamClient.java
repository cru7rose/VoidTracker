package com.example.planning_service.client;

import com.example.planning_service.dto.UserInfoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "iam-service", url = "${application.config.iam-service-url:http://localhost:8090}")
public interface IamClient {

    @PostMapping("/api/auth/magic-link/generate")
    Map<String, String> generateMagicLink(@RequestParam("email") String email);

    /**
     * Get user information by ID
     * GET /api/users/{userId}
     */
    @GetMapping("/api/users/{userId}")
    UserInfoDto getUserById(@PathVariable("userId") UUID userId);
}
