package com.blog.notification.controller;

import com.blog.notification.application.dto.request.CreateNotificationsRequest;
import com.blog.notification.application.dto.response.NotificationResponse;
import com.blog.notification.service.NotificationService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Not routed through the API gateway: only other services publish notifications about their own events.
 */
@RestController
@RequestMapping("/internal/notifications")
public class InternalNotificationController {

    private final NotificationService notificationService;

    public InternalNotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<List<NotificationResponse>> create(
            @Valid @RequestBody CreateNotificationsRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.create(request.notifications()));
    }
}
