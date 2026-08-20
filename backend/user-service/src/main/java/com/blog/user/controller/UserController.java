package com.blog.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blog.user.application.dto.request.CreateUserRequest;
import com.blog.user.application.dto.response.UserResponse;
import com.blog.user.service.UserService;
import com.blog.user.domain.entity.User;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    // intialize the @Service Object
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // signup
    @PostMapping("signup")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = userService.createUser(request);
        UserResponse response = new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getAge(),
                user.getGender(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // test if the service is up
    @GetMapping("ping")
    public String ping() {
        return "pong";
    }
}