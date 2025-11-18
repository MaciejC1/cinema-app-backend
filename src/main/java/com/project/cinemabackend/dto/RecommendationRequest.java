package com.project.cinemabackend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RecommendationRequest {
    private UUID userId;
    private int topN = 10;
}
