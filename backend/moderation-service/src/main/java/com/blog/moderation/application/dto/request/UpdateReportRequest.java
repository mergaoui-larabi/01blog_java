package com.blog.moderation.application.dto.request;

import com.blog.moderation.domain.model.ReportStatus;

import jakarta.validation.constraints.NotNull;

public record UpdateReportRequest(
        @NotNull(message = "status is required") ReportStatus status) {
}
