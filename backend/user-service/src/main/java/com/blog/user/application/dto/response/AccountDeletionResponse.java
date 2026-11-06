package com.blog.user.application.dto.response;

import java.util.List;
import java.util.UUID;

import com.blog.user.domain.model.AccountStatus;

public record AccountDeletionResponse(
        UUID userId,
        AccountStatus status,
        List<String> pendingServices) {

    public static AccountDeletionResponse deleted(UUID userId) {
        return new AccountDeletionResponse(userId, AccountStatus.DELETED, List.of());
    }

    public static AccountDeletionResponse pending(UUID userId, List<String> pendingServices) {
        return new AccountDeletionResponse(userId, AccountStatus.DELETION_PENDING, pendingServices);
    }
}
