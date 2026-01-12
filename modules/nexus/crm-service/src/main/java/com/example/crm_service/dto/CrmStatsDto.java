package com.example.crm_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CrmStatsDto {
    private Double averageRating;
    private Long totalReviews;
    private Integer nps; // Net Promoter Score (-100 to 100)
}
