package com.blog.moderation.controller;

import com.blog.moderation.application.dto.response.UserCleanupResponse;
import com.blog.moderation.service.ReportCleanupService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Not routed through the API gateway: User Service calls this while orchestrating an account deletion.
 */
@RestController
@RequestMapping("/internal/users")
public class InternalUserCleanupController {

    private final ReportCleanupService reportCleanupService;

    public InternalUserCleanupController(ReportCleanupService reportCleanupService) {
        this.reportCleanupService = reportCleanupService;
    }

    @PostMapping("/{userId}/cleanup")
    public ResponseEntity<UserCleanupResponse> cleanup(@PathVariable UUID userId) {
        return ResponseEntity.ok(reportCleanupService.cleanup(userId));
    }
}
