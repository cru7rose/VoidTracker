package com.example.order_service.service;

import com.example.order_service.dto.VisionAnalysisResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class VisionAnalysisService {

    public VisionAnalysisResponseDto analyzeImage(MultipartFile file, java.util.UUID orderId) {
        List<String> tags = new ArrayList<>();
        String condition = "GOOD";
        Double confidence = 0.95;

        if (file == null || file.isEmpty()) {
            return new VisionAnalysisResponseDto(tags, "UNKNOWN", 0.0);
        }

        String filename = file.getOriginalFilename();
        if (filename != null && filename.toLowerCase().contains("damage")) {
            condition = "DAMAGED";
            tags.add("scraches");
            tags.add("dent");
            confidence = 0.88;
        } else {
            tags.add("box");
            tags.add("label_detected");
        }

        if (orderId != null) {
            updateOrderCondition(orderId, condition, tags);
        }

        return new VisionAnalysisResponseDto(tags, condition, confidence);
    }

    // Mock dependencies for now - in real life we would inject
    // OrderRepository/AssetRepository
    // private final OrderRepository orderRepository;

    private void updateOrderCondition(java.util.UUID orderId, String condition, List<String> tags) {
        System.out.println(
                "[MOCK DB UPDATE] Order " + orderId + " condition set to " + condition + " with tags: " + tags);
        // In real impl:
        // OrderEntity order = orderRepository.findById(orderId).orElseThrow();
        // order.getDeliveryAddress().setNote("Condition: " + condition);
        // orderRepository.save(order);
    }
}
