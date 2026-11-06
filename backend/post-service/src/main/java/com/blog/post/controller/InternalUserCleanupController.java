package com.blog.post.controller;

import com.blog.post.application.dto.UserCleanupResponse;
import com.blog.post.service.UserCleanupService;

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

    private final UserCleanupService userCleanupService;

    public InternalUserCleanupController(UserCleanupService userCleanupService) {
        this.userCleanupService = userCleanupService;
    }

    @PostMapping("/{userId}/cleanup")
    public ResponseEntity<UserCleanupResponse> cleanup(@PathVariable UUID userId) {
        return ResponseEntity.ok(userCleanupService.cleanup(userId));
    }
}
