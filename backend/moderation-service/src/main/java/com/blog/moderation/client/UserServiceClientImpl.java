package com.blog.moderation.client;

import java.util.Optional;
import java.util.UUID;

import com.blog.moderation.client.dto.UserRoleResponse;
import com.blog.moderation.exception.UserServiceUnavailableException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
public class UserServiceClientImpl implements UserServiceClient {

    private static final Logger log = LoggerFactory.getLogger(UserServiceClientImpl.class);

    private final RestClient restClient;

    public UserServiceClientImpl(@Qualifier("userServiceRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public Optional<UserRoleResponse> findUserRole(UUID userId) {
        try {
            UserRoleResponse response = restClient.get()
                    .uri("/internal/users/{userId}/role", userId)
                    .retrieve()
                    .body(UserRoleResponse.class);

            if (response == null || response.role() == null || response.role().isBlank()) {
                throw new UserServiceUnavailableException("User Service returned an unexpected role response");
            }

            return Optional.of(response);
        } catch (ResourceAccessException exception) {
            log.error("Failed to reach User Service for role of {}: {}", userId, exception.getMessage());
            throw new UserServiceUnavailableException("User Service is currently unavailable", exception);
        } catch (RestClientResponseException exception) {
            int statusCode = exception.getStatusCode().value();
            if (statusCode == HttpStatus.NOT_FOUND.value() || statusCode == HttpStatus.FORBIDDEN.value()) {
                return Optional.empty();
            }
            log.error("User Service returned status {} for role of {}", statusCode, userId);
            throw new UserServiceUnavailableException("User Service returned error: " + statusCode, exception);
        }
    }
}
