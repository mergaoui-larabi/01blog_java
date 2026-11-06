package com.blog.user.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.blog.user.application.dto.response.AccountDeletionResponse;
import com.blog.user.client.CleanupTarget;
import com.blog.user.client.UserCleanupClient;

/**
 * Coordinates account deletion over REST. The class carries no transaction: each state change is its
 * own short transaction and the cleanup calls happen with no database transaction open, so a slow or
 * unavailable service can never pin a connection.
 */
@Service
public class UserDeletionService {

    private static final Logger log = LoggerFactory.getLogger(UserDeletionService.class);

    private final UserService userService;
    private final FollowerService followerService;
    private final UserCleanupClient userCleanupClient;

    public UserDeletionService(
            UserService userService,
            FollowerService followerService,
            UserCleanupClient userCleanupClient) {
        this.userService = userService;
        this.followerService = followerService;
        this.userCleanupClient = userCleanupClient;
    }

    public AccountDeletionResponse deleteAccount(UUID userId) {
        // Idempotent: a retry after a partial failure re-marks the same DELETION_PENDING account.
        userService.markDeletionPending(userId);
        followerService.removeAllForUser(userId);

        List<String> pendingServices = new ArrayList<>();
        for (CleanupTarget target : userCleanupClient.targets()) {
            try {
                userCleanupClient.cleanup(target, userId);
            } catch (RuntimeException exception) {
                log.warn("Cleanup of user {} failed in {}: {}",
                        userId, target.serviceName(), exception.getMessage());
                pendingServices.add(target.serviceName());
            }
        }

        if (!pendingServices.isEmpty()) {
            log.warn("Deletion of user {} is pending, cleanup still failing for {}", userId, pendingServices);
            return AccountDeletionResponse.pending(userId, pendingServices);
        }

        userService.markDeleted(userId);
        return AccountDeletionResponse.deleted(userId);
    }
}
