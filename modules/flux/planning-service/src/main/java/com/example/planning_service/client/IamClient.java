package com.example.planning_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "iam-service", url = "${application.config.iam-service-url:http://localhost:8081}")
public interface IamClient {

    @PostMapping("/api/auth/magic-link/generate")
    Map<String, String> generateMagicLink(@RequestParam("email") String email);
}
