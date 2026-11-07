package com.blog.notification.controller;

import com.blog.notification.application.dto.request.UpdateNotificationReadRequest;
import com.blog.notification.application.dto.response.NotificationResponse;
import com.blog.notification.application.dto.response.PageResponse;
import com.blog.notification.application.dto.response.UnreadCountResponse;
import com.blog.notification.service.NotificationService;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@Validated
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<NotificationResponse>> fetchNotifications(
            @RequestParam(required = false) UUID cursor,
            @RequestParam(required = false, defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(notificationService.fetchNotifications(userId(jwt), cursor, size, unreadOnly));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountResponse> countUnread(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(new UnreadCountResponse(notificationService.countUnread(userId(jwt))));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllRead(@AuthenticationPrincipal Jwt jwt) {
        notificationService.markAllRead(userId(jwt));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{notificationId}")
    public ResponseEntity<NotificationResponse> updateReadState(
            @PathVariable UUID notificationId,
            @Valid @RequestBody UpdateNotificationReadRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(notificationService.updateReadState(notificationId, userId(jwt), request.read()));
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable UUID notificationId,
            @AuthenticationPrincipal Jwt jwt) {
        notificationService.delete(notificationId, userId(jwt));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("ping")
    public String ping() {
        return "pong";
    }

    private UUID userId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
