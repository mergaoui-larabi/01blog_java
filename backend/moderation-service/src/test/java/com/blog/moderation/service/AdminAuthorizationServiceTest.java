package com.blog.moderation.service;

import com.blog.moderation.client.UserServiceClient;
import com.blog.moderation.client.dto.UserRoleResponse;
import com.blog.moderation.exception.AdminOnlyException;
import com.blog.moderation.exception.UserServiceUnavailableException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAuthorizationServiceTest {

    private static final UUID CALLER_ID = UUID.randomUUID();

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private AdminAuthorizationService adminAuthorizationService;

    @Test
    void requireAdminAllowsCurrentAdministrator() {
        givenUserRole("ADMIN", false);

        assertThat(adminAuthorizationService.requireAdmin(jwt("ADMIN"))).isEqualTo(CALLER_ID);
    }

    @Test
    void requireAdminDeniesUserWhoseStaleJwtStillClaimsAdmin() {
        givenUserRole("USER", false);

        assertThatThrownBy(() -> adminAuthorizationService.requireAdmin(jwt("ADMIN")))
                .isInstanceOf(AdminOnlyException.class);
    }

    @Test
    void requireAdminDeniesBannedAdministrator() {
        givenUserRole("ADMIN", true);

        assertThatThrownBy(() -> adminAuthorizationService.requireAdmin(jwt("ADMIN")))
                .isInstanceOf(AdminOnlyException.class);
    }

    @Test
    void requireAdminDeniesUnknownUser() {
        when(userServiceClient.findUserRole(CALLER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminAuthorizationService.requireAdmin(jwt("ADMIN")))
                .isInstanceOf(AdminOnlyException.class);
    }

    @Test
    void requireAdminFailsClosedWhenUserServiceIsUnavailable() {
        when(userServiceClient.findUserRole(CALLER_ID))
                .thenThrow(new UserServiceUnavailableException("User Service is currently unavailable"));

        assertThatThrownBy(() -> adminAuthorizationService.requireAdmin(jwt("ADMIN")))
                .isInstanceOf(UserServiceUnavailableException.class);
    }

    @Test
    void requireAdminDeniesJwtWithoutUserIdSubject() {
        Jwt anonymous = new Jwt("token", Instant.now(), Instant.now().plusSeconds(60),
                Map.of("alg", "RS256"), Map.of("role", "ADMIN"));

        assertThatThrownBy(() -> adminAuthorizationService.requireAdmin(anonymous))
                .isInstanceOf(AdminOnlyException.class);
    }

    private void givenUserRole(String role, boolean banned) {
        when(userServiceClient.findUserRole(CALLER_ID))
                .thenReturn(Optional.of(new UserRoleResponse(CALLER_ID, role, banned)));
    }

    private Jwt jwt(String role) {
        return new Jwt("token", Instant.now(), Instant.now().plusSeconds(60),
                Map.of("alg", "RS256"), Map.of("sub", CALLER_ID.toString(), "role", role));
    }
}
