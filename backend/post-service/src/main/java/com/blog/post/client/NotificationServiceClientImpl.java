package com.blog.post.client;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.blog.post.client.dto.PostCreatedPayload;
import com.blog.post.exception.NotificationServiceUnavailableException;

@Service
public class NotificationServiceClientImpl implements NotificationServiceClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceClientImpl.class);
    private final RestClient restClient;

    public NotificationServiceClientImpl(@Qualifier("notificationServiceRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public void notifyPostCreated(UUID postId, UUID authorId) {
        try {
            restClient.post()
                    .uri("/internal/notifications/post-created")
                    .body(new PostCreatedPayload(postId, authorId))
                    .retrieve()
                    .toBodilessEntity();
        } catch (ResourceAccessException exception) {
            log.error("Failed to connect to Notification Service at /internal/notifications/post-created: {}",
                    exception.getMessage());
            throw new NotificationServiceUnavailableException("Notification Service is currently unavailable",
                    exception);
        } catch (RestClientResponseException exception) {
            log.error("Notification Service returned status {} for post {}: {}",
                    exception.getStatusCode(), postId, exception.getMessage());
            throw new NotificationServiceUnavailableException(
                    "Notification Service returned error: " + exception.getStatusCode(), exception);
        }
    }
}
