package com.blog.user.client;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.blog.user.exception.ServiceCleanupUnavailableException;

@Service
public class UserCleanupClient {

    private static final Logger log = LoggerFactory.getLogger(UserCleanupClient.class);

    private final List<CleanupTarget> targets;

    public UserCleanupClient(
            @Qualifier("postServiceRestClient") RestClient postServiceRestClient,
            @Qualifier("notificationServiceRestClient") RestClient notificationServiceRestClient,
            @Qualifier("moderationServiceRestClient") RestClient moderationServiceRestClient) {
        this.targets = List.of(
                new CleanupTarget("post-service", postServiceRestClient),
                new CleanupTarget("notification-service", notificationServiceRestClient),
                new CleanupTarget("moderation-service", moderationServiceRestClient));
    }

    public List<CleanupTarget> targets() {
        return targets;
    }

    public void cleanup(CleanupTarget target, UUID userId) {
        try {
            target.restClient().post()
                    .uri("/internal/users/{userId}/cleanup", userId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (ResourceAccessException exception) {
            log.error("{} is unreachable for cleanup of user {}: {}",
                    target.serviceName(), userId, exception.getMessage());
            throw new ServiceCleanupUnavailableException(target.serviceName(), exception);
        } catch (RestClientResponseException exception) {
            log.error("{} returned status {} for cleanup of user {}",
                    target.serviceName(), exception.getStatusCode(), userId);
            throw new ServiceCleanupUnavailableException(target.serviceName(), exception);
        }
    }
}
