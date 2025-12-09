package com.project.cinemabackend.dto;

import com.project.cinemabackend.model.AudioTrackType;

import java.time.LocalDateTime;
import java.util.UUID;

public record ShowtimeDTO(
        UUID id,
        LocalDateTime startTime,
        boolean is3d,
        String language,
        String subtitles,
        boolean hasSubtitles,
        AudioTrackType audioTrack,
        String hallName
) {}
