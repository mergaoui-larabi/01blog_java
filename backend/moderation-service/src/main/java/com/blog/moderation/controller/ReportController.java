package com.blog.moderation.controller;

import com.blog.moderation.application.dto.request.CreatePostReportRequest;
import com.blog.moderation.application.dto.request.CreateUserReportRequest;
import com.blog.moderation.application.dto.request.UpdateReportRequest;
import com.blog.moderation.application.dto.response.PageResponse;
import com.blog.moderation.application.dto.response.ReportResponse;
import com.blog.moderation.domain.model.ReportStatus;
import com.blog.moderation.service.AdminAuthorizationService;
import com.blog.moderation.service.ReportService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
@Validated
public class ReportController {

    private final ReportService reportService;
    private final AdminAuthorizationService adminAuthorizationService;

    public ReportController(ReportService reportService, AdminAuthorizationService adminAuthorizationService) {
        this.reportService = reportService;
        this.adminAuthorizationService = adminAuthorizationService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<ReportResponse>> fetchReports(
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(required = false) UUID cursor,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @AuthenticationPrincipal Jwt jwt) {
        adminAuthorizationService.requireAdmin(jwt);
        return ResponseEntity.ok(reportService.fetchReports(status, cursor, size));
    }

    @PostMapping("/posts")
    public ResponseEntity<ReportResponse> createPostReport(
            @Valid @RequestBody CreatePostReportRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reportService.createPostReport(request, userId(jwt)));
    }

    @PostMapping("/users")
    public ResponseEntity<ReportResponse> createUserReport(
            @Valid @RequestBody CreateUserReportRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reportService.createUserReport(request, userId(jwt)));
    }

    @PatchMapping("/{reportId}")
    public ResponseEntity<ReportResponse> updateReport(
            @PathVariable UUID reportId,
            @Valid @RequestBody UpdateReportRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID moderatorId = adminAuthorizationService.requireAdmin(jwt);
        return ResponseEntity.ok(reportService.updateReport(reportId, request, moderatorId));
    }

    @DeleteMapping("/{reportId}")
    public ResponseEntity<Void> deleteReport(
            @PathVariable UUID reportId,
            @AuthenticationPrincipal Jwt jwt) {
        adminAuthorizationService.requireAdmin(jwt);
        reportService.deleteReport(reportId);
        return ResponseEntity.noContent().build();
    }

    private UUID userId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
