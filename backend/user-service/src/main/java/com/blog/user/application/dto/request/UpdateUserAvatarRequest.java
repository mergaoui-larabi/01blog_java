package com.blog.user.application.dto.request;

import java.util.UUID;

public record UpdateUserAvatarRequest(
        UUID ownerId,
        UUID avatarMediaId) {
}