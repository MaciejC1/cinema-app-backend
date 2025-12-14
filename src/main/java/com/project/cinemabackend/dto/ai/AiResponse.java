package com.project.cinemabackend.dto.ai;

import java.util.UUID;

public record AiResponse(
        UUID movieId,
        String title,
        Integer match_percent,
        Double predicted_rating
) {}
