package com.example.crm_service.service;

import com.example.crm_service.dto.CrmStatsDto;
import com.example.crm_service.entity.ClientFeedbackEntity;
import com.example.crm_service.repository.ClientFeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CrmStatsService {

    private final ClientFeedbackRepository feedbackRepository;

    public CrmStatsDto getSatisfactionStats() {
        List<ClientFeedbackEntity> allFeedback = feedbackRepository.findAll();

        if (allFeedback.isEmpty()) {
            return CrmStatsDto.builder()
                    .averageRating(0.0)
                    .totalReviews(0L)
                    .nps(0)
                    .build();
        }

        double totalRating = 0;
        int promoters = 0;
        int detractors = 0;

        for (ClientFeedbackEntity feedback : allFeedback) {
            Integer rating = feedback.getRating(); // 1-5
            if (rating != null) {
                totalRating += rating;

                // NPS Approximation using 5-star scale
                // 5 = Promoter
                // 4 = Passive
                // 1-3 = Detractor
                if (rating == 5)
                    promoters++;
                else if (rating <= 3)
                    detractors++;
            }
        }

        double avg = totalRating / allFeedback.size();

        // NPS Formula: %Promoters - %Detractors
        double nps = ((double) (promoters - detractors) / allFeedback.size()) * 100;

        return CrmStatsDto.builder()
                .averageRating(Math.round(avg * 10.0) / 10.0) // Round to 1 decimal
                .totalReviews((long) allFeedback.size())
                .nps((int) nps)
                .build();
    }
}
