package com.blog.post.client;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.blog.post.exception.InvalidMediaException;
import com.blog.post.exception.MediaServiceUnavailableException;

@Service
public class MediaServiceClientImpl implements MediaServiceClient {

    private static final Logger log = LoggerFactory.getLogger(MediaServiceClientImpl.class);
    private final RestClient restClient;

    public MediaServiceClientImpl(@Qualifier("mediaServiceRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public void validateMediaOwnership(UUID mediaId, String authorizationHeader) {
        if (mediaId == null) {
            return;
        }

        try {
            var request = restClient.get()
                    .uri("/api/v1/medias/{mediaId}", mediaId);

            if (authorizationHeader != null && !authorizationHeader.isBlank()) {
                request.header(HttpHeaders.AUTHORIZATION, authorizationHeader);
            }

            request.retrieve().toBodilessEntity();
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode() == HttpStatus.NOT_FOUND || exception.getStatusCode() == HttpStatus.FORBIDDEN) {
                log.warn("Media validation failed for mediaId {}: status {}", mediaId, exception.getStatusCode());
                throw new InvalidMediaException("Media attachment is invalid or not owned by user: " + mediaId);
            }
            log.error("Media Service returned status {} for mediaId {}: {}", exception.getStatusCode(), mediaId, exception.getMessage());
            throw new MediaServiceUnavailableException("Media Service returned error: " + exception.getStatusCode(), exception);
        } catch (ResourceAccessException exception) {
            log.error("Failed to connect to Media Service at /api/v1/medias/{}: {}", mediaId, exception.getMessage());
            throw new MediaServiceUnavailableException("Media Service is currently unavailable", exception);
        }
    }

    @Override
    public void validateAllMediaOwnership(List<UUID> mediaIds, String authorizationHeader) {
        if (mediaIds == null || mediaIds.isEmpty()) {
            return;
        }
        for (UUID mediaId : mediaIds) {
            validateMediaOwnership(mediaId, authorizationHeader);
        }
    }
}
