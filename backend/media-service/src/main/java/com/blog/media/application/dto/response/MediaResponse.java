package com.blog.media.application.dto.response;

import java.time.Instant;
import java.util.UUID;

import com.blog.media.domain.model.MediaPurpose;
import com.blog.media.domain.model.MediaStatus;

public record MediaResponse(
        UUID id,
        UUID ownerId,
        MediaPurpose purpose,
        MediaStatus status,
        String originalFilename,
        String detectedContentType,
        long sizeBytes,
        Integer width,
        Integer height,
        Long durationMs,
        Instant createdAt,
        Instant updatedAt) {
}
