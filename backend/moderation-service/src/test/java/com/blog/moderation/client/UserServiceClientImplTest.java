package com.blog.moderation.client;

import java.util.UUID;

import com.blog.moderation.client.dto.UserRoleResponse;
import com.blog.moderation.exception.UserServiceUnavailableException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpStatus.FORBIDDEN;

class UserServiceClientImplTest {

    private static final String BASE_URL = "http://user-service";
    private static final UUID USER_ID = UUID.randomUUID();

    private MockRestServiceServer server;
    private UserServiceClientImpl client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        client = new UserServiceClientImpl(builder.build());
    }

    @Test
    void findUserRoleReadsTheInternalRoleEndpoint() {
        server.expect(requestTo(BASE_URL + "/internal/users/" + USER_ID + "/role"))
                .andRespond(withSuccess("{\"userId\":\"" + USER_ID + "\",\"role\":\"ADMIN\",\"banned\":false}",
                        MediaType.APPLICATION_JSON));

        assertThat(client.findUserRole(USER_ID))
                .contains(new UserRoleResponse(USER_ID, "ADMIN", false));
        server.verify();
    }

    @Test
    void findUserRoleReturnsEmptyWhenUserDoesNotExist() {
        server.expect(requestTo(BASE_URL + "/internal/users/" + USER_ID + "/role"))
                .andRespond(withStatus(FORBIDDEN));

        assertThat(client.findUserRole(USER_ID)).isEmpty();
    }

    @Test
    void findUserRoleFailsClosedOnServerError() {
        server.expect(requestTo(BASE_URL + "/internal/users/" + USER_ID + "/role"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.findUserRole(USER_ID))
                .isInstanceOf(UserServiceUnavailableException.class);
    }

    @Test
    void findUserRoleFailsClosedOnUnexpectedBody() {
        server.expect(requestTo(BASE_URL + "/internal/users/" + USER_ID + "/role"))
                .andRespond(withSuccess("{\"userId\":\"" + USER_ID + "\"}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.findUserRole(USER_ID))
                .isInstanceOf(UserServiceUnavailableException.class);
    }
}
