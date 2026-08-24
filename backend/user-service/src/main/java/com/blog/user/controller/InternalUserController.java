package com.blog.user.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blog.user.application.dto.request.AuthenticateUserRequest;
import com.blog.user.application.dto.response.UserAuthenticationResponse;
import com.blog.user.service.UserService;

@RestController
@RequestMapping("/internal/users")
public class InternalUserController {

    private final UserService userService;

    public InternalUserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/authenticate")
    public UserAuthenticationResponse authenticate(
            @RequestBody AuthenticateUserRequest request) {

        return userService.authenticate(request);
    }
}