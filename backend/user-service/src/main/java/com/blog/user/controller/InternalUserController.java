package com.blog.user.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blog.user.application.dto.request.AuthenticateUserRequest;
import com.blog.user.application.dto.response.FollowedUsersResponse;
import com.blog.user.application.dto.response.UserAuthenticationResponse;
import com.blog.user.service.FollowerService;
import com.blog.user.service.UserService;

@RestController
@RequestMapping("/internal/users")
public class InternalUserController {

    private final UserService userService;
    private final FollowerService followerService;

    public InternalUserController(UserService userService, FollowerService followerService) {
        this.userService = userService;
        this.followerService = followerService;
    }

    @PostMapping("/authenticate")
    public UserAuthenticationResponse authenticate(
            @RequestBody AuthenticateUserRequest request) {

        return userService.authenticate(request);
    }

    @GetMapping("/{userId}/following")
    public FollowedUsersResponse getFollowedUsers(@PathVariable UUID userId) {
        List<UUID> followedUserIds = followerService.getFollowedUserIds(userId);
        return new FollowedUsersResponse(userId, followedUserIds);
    }
}
