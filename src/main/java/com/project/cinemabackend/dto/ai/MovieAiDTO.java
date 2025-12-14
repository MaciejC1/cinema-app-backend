package com.project.cinemabackend.dto.ai;

import java.util.List;
import java.util.UUID;

public record MovieAiDTO(
        UUID id,
        String title,
        List<String> genres,
        List<String> tags
) {}
