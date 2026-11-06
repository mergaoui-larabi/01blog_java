package com.blog.notification.client;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.blog.notification.client.dto.FollowerIdsResponse;
import com.blog.notification.exception.UserServiceUnavailableException;

@Service
public class UserServiceClientImpl implements UserServiceClient {

    private static final Logger log = LoggerFactory.getLogger(UserServiceClientImpl.class);
    private final RestClient restClient;

    public UserServiceClientImpl(@Qualifier("userServiceRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public List<UUID> getFollowerIds(UUID userId) {
        try {
            FollowerIdsResponse response = restClient.get()
                    .uri("/internal/users/{userId}/followers", userId)
                    .retrieve()
                    .body(FollowerIdsResponse.class);

            if (response == null || response.followerIds() == null) {
                return List.of();
            }

            return response.followerIds();
        } catch (ResourceAccessException exception) {
            log.error("Failed to connect to User Service at /internal/users/{}/followers: {}",
                    userId, exception.getMessage());
            throw new UserServiceUnavailableException("User Service is currently unavailable", exception);
        } catch (RestClientResponseException exception) {
            log.error("User Service returned status {} for user {}: {}",
                    exception.getStatusCode(), userId, exception.getMessage());
            throw new UserServiceUnavailableException("User Service returned error: " + exception.getStatusCode(),
                    exception);
        }
    }
}
