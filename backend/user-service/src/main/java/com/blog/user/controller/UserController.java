package com.blog.user.controller;

import java.util.UUID;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blog.user.application.dto.request.CreateUserRequest;
import com.blog.user.application.dto.request.UpdateUserAvatarRequest;
import com.blog.user.application.dto.response.UserResponse;
import com.blog.user.service.FollowerService;
import com.blog.user.service.UserService;
import com.blog.user.domain.entity.User;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final FollowerService followerService;

    // intialize the @Service Object
    public UserController(UserService userService, FollowerService followerService) {
        this.userService = userService;
        this.followerService = followerService;
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
                user.getAvatarMediaId(),
                user.getCreatedAt(),
                user.getUpdatedAt());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // update
    @PatchMapping("update")
    public ResponseEntity<UserResponse> updateUserAvatar(@Valid @RequestBody UpdateUserAvatarRequest request) {
        User user = userService.updateAvatarMediaId(request);
        UserResponse response = new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getAge(),
                user.getGender(),
                user.getRole(),
                user.getAvatarMediaId(),
                user.getCreatedAt(),
                user.getUpdatedAt());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // user profile
    @GetMapping("me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());

        User user = userService.getUserById(userId);

        UserResponse response = new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getAge(),
                user.getGender(),
                user.getRole(),
                user.getAvatarMediaId(),
                user.getCreatedAt(),
                user.getUpdatedAt());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}/follow")
    public ResponseEntity<Void> follow(
            @PathVariable UUID userId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID currentUserId = UUID.fromString(jwt.getSubject());
        followerService.follow(currentUserId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}/follow")
    public ResponseEntity<Void> unfollow(
            @PathVariable UUID userId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID currentUserId = UUID.fromString(jwt.getSubject());
        followerService.unfollow(currentUserId, userId);
        return ResponseEntity.noContent().build();
    }

    // test if the service is up
    @GetMapping("ping")
    public String ping() {
        return "pong";
    }
}