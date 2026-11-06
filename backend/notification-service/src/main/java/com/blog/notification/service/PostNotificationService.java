package com.blog.notification.service;

import com.blog.notification.application.dto.request.CreateNotificationRequest;
import com.blog.notification.application.dto.response.NotificationResponse;
import com.blog.notification.client.UserServiceClient;
import com.blog.notification.domain.model.NotificationType;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Turns a post-created event into one NEW_POST notification per follower. The follower lookup is a
 * network call, so it deliberately runs outside the persistence transaction that
 * NotificationService#create opens.
 */
@Service
public class PostNotificationService {

    private final UserServiceClient userServiceClient;
    private final NotificationService notificationService;

    public PostNotificationService(
            UserServiceClient userServiceClient,
            NotificationService notificationService) {
        this.userServiceClient = userServiceClient;
        this.notificationService = notificationService;
    }

    public List<NotificationResponse> notifyPostCreated(UUID postId, UUID authorId) {
        List<CreateNotificationRequest> requests = userServiceClient.getFollowerIds(authorId).stream()
                .filter(recipientId -> recipientId != null && !recipientId.equals(authorId))
                .distinct()
                .map(recipientId -> new CreateNotificationRequest(
                        recipientId,
                        authorId,
                        NotificationType.NEW_POST,
                        postId,
                        null))
                .toList();

        if (requests.isEmpty()) {
            return List.of();
        }

        return notificationService.create(requests);
    }
}
