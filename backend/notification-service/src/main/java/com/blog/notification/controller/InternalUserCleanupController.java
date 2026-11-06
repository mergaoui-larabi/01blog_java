package com.blog.notification.controller;

import com.blog.notification.application.dto.response.UserCleanupResponse;
import com.blog.notification.service.NotificationService;

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

    private final NotificationService notificationService;

    public InternalUserCleanupController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/{userId}/cleanup")
    public ResponseEntity<UserCleanupResponse> cleanup(@PathVariable UUID userId) {
        return ResponseEntity.ok(new UserCleanupResponse(userId, notificationService.cleanupUser(userId)));
    }
}
