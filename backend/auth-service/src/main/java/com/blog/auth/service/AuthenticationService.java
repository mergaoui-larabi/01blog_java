package com.blog.auth.service;

import org.springframework.stereotype.Service;

import com.blog.auth.application.client.UserServiceClient;
import com.blog.auth.application.dto.request.LoginRequest;
import com.blog.auth.application.dto.response.LoginResponse;
import com.blog.auth.application.dto.user.AuthenticateUserRequest;
import com.blog.auth.application.dto.user.UserAuthenticationResponse;
import com.blog.auth.security.JwtService;

@Service
public class AuthenticationService {

    private final UserServiceClient userServiceClient;
    private final JwtService jwtService;

    public AuthenticationService(
            UserServiceClient userServiceClient,
            JwtService jwtService) {

        this.userServiceClient = userServiceClient;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {

        UserAuthenticationResponse user =
                userServiceClient.authenticate(
                        new AuthenticateUserRequest(
                                request.email(),
                                request.password()
                        ));

        String token = jwtService.createToken(
                user.userId(),
                user.role()
        );

        return new LoginResponse(
                token,
                "Bearer",
                3600
        );
    }
}