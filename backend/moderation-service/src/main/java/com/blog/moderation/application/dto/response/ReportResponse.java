package com.blog.moderation.application.dto.response;

import com.blog.moderation.domain.entity.Report;
import com.blog.moderation.domain.model.ReportStatus;
import com.blog.moderation.domain.model.TargetType;

import java.time.Instant;
import java.util.UUID;

public record ReportResponse(
        UUID id,
        UUID reporterId,
        TargetType targetType,
        UUID targetId,
        String reason,
        ReportStatus status,
        Instant createdAt,
        Instant updatedAt) {

    public static ReportResponse from(Report report) {
        return new ReportResponse(
                report.getId(),
                report.getReporterId(),
                report.getTargetType(),
                report.getTargetId(),
                report.getReason(),
                report.getStatus(),
                report.getCreatedAt(),
                report.getUpdatedAt());
    }
}
