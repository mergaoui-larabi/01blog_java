package com.blog.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.blog.auth.application.client.UserServiceClient;
import com.blog.auth.application.dto.user.AuthenticateUserRequest;
import com.blog.auth.application.dto.user.UserAuthenticationResponse;
import com.blog.auth.exception.InvalidCredentialsException;
import com.blog.auth.exception.InvalidRequestException;
import com.blog.auth.exception.UserServiceUnavailableException;

@Service
public class UserServiceClientImpl implements UserServiceClient {

    private static final String SERVICE_UNAVAILABLE_MESSAGE = "The user service is currently unavailable";

    private final RestClient restClient;

    public UserServiceClientImpl(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public UserAuthenticationResponse authenticate(AuthenticateUserRequest request) {
        try {
            UserAuthenticationResponse response = restClient
                    .post()
                    .uri("/internal/users/authenticate")
                    .body(request)
                    .retrieve()
                    .body(UserAuthenticationResponse.class);

            if (response == null) {
                throw new UserServiceUnavailableException(SERVICE_UNAVAILABLE_MESSAGE);
            }

            return response;
        } catch (RestClientResponseException exception) {
            throw mapErrorResponse(exception);
        } catch (ResourceAccessException exception) {
            // connection refused / DNS failure / read timeout on the user service
            throw new UserServiceUnavailableException(SERVICE_UNAVAILABLE_MESSAGE, exception);
        }
    }

    private RuntimeException mapErrorResponse(RestClientResponseException exception) {
        return switch (exception.getStatusCode().value()) {
            case 400 -> new InvalidRequestException("The login request is invalid");
            case 401, 403 -> new InvalidCredentialsException("Invalid email or password");
            default -> new UserServiceUnavailableException(SERVICE_UNAVAILABLE_MESSAGE, exception);
        };
    }
}
