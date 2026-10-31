package com.blog.moderation.service;

import com.blog.moderation.client.UserServiceClient;
import com.blog.moderation.client.dto.UserRoleResponse;
import com.blog.moderation.constant.SecurityConstants;
import com.blog.moderation.exception.AdminOnlyException;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Resolves the caller's CURRENT role from User Service instead of trusting the
 * role claim carried by the stateless JWT, which stays valid until it expires
 * even after the administrator role is revoked.
 */
@Service
public class AdminAuthorizationService {

    private final UserServiceClient userServiceClient;

    public AdminAuthorizationService(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    public UUID requireAdmin(Jwt jwt) {
        UUID callerId = callerId(jwt);

        UserRoleResponse user = userServiceClient.findUserRole(callerId)
                .orElseThrow(AdminOnlyException::new);

        if (Boolean.TRUE.equals(user.banned()) || !SecurityConstants.ADMIN_ROLE.equals(user.role())) {
            throw new AdminOnlyException();
        }

        return callerId;
    }

    private UUID callerId(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null) {
            throw new AdminOnlyException();
        }

        try {
            return UUID.fromString(jwt.getSubject());
        } catch (IllegalArgumentException exception) {
            throw new AdminOnlyException();
        }
    }
}
