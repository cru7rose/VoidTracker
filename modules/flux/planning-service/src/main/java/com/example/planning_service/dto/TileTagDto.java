package com.example.planning_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TileTagDto {
    private UUID id;
    private String tagKey;
    private String label;
    private String color;
    private String icon;
    private String rulesJson;
}
