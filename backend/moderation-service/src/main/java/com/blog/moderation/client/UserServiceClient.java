package com.blog.moderation.client;

import java.util.Optional;
import java.util.UUID;

import com.blog.moderation.client.dto.UserRoleResponse;

public interface UserServiceClient {

    Optional<UserRoleResponse> findUserRole(UUID userId);
}
