package com.blog.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.blog.auth.application.client.UserServiceClient;
import com.blog.auth.application.dto.user.AuthenticateUserRequest;
import com.blog.auth.application.dto.user.UserAuthenticationResponse;

@Service
public class UserServiceClientImpl implements UserServiceClient {

    private final RestClient restClient;

    public UserServiceClientImpl(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public UserAuthenticationResponse authenticate(AuthenticateUserRequest request) {
        return restClient
                .post()
                .uri("/internal/users/authenticate")
                .body(request)
                .retrieve()
                .body(UserAuthenticationResponse.class);
    }
}
