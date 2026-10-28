package com.blog.user.controller;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.blog.user.application.dto.request.CreateUserRequest;
import com.blog.user.application.dto.request.PageResponse;
import com.blog.user.application.dto.request.UpdateUserAvatarRequest;
import com.blog.user.application.dto.response.UserResponse;
import com.blog.user.application.dto.response.UserSearchResponse;
import com.blog.user.exception.InvalidValueException;
import com.blog.user.service.FollowerService;
import com.blog.user.service.UserService;
import com.blog.user.domain.entity.User;
import com.blog.user.domain.valueobject.Username;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final FollowerService followerService;

    public UserController(UserService userService, FollowerService followerService) {
        this.userService = userService;
        this.followerService = followerService;
    }

    @GetMapping("/me")
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
                user.getBanned(),
                user.getCreatedAt(),
                user.getUpdatedAt());

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<UserSearchResponse>> getUsersList(
            @RequestParam(required = false) UUID cursor,
            @RequestParam(required = false) Instant cursorInstant,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @AuthenticationPrincipal Jwt jwt) {

        Page<User> users = userService.getUsersList(cursor, cursorInstant, search, size, jwt);
        String subject = jwt.getSubject();
        List<UserSearchResponse> content = users.getContent()
                .stream()
                .map(u -> {
                    return new UserSearchResponse(
                            u.getId(),
                            u.getEmail(),
                            u.getUsername(),
                            u.getAge(),
                            u.getGender(),
                            u.getRole(),
                            u.getAvatarMediaId(),
                            u.getBanned(),
                            followerService.isFollowing(UUID.fromString(subject), u.getId()),
                            u.getCreatedAt(),
                            u.getUpdatedAt());

                })
                .toList();

        String nextCursor = null;
        if (!content.isEmpty() && users.getTotalPages() > 0) {
            long fetched = users.getContent().size();
            if (fetched > size) {
                User lastFetched = users.getContent().get(size - 1);
                nextCursor = lastFetched.getCreatedAt().toString();
            }
        }

        PageResponse<UserSearchResponse> response = new PageResponse<>(
                content,
                nextCursor);

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

    @PatchMapping("/{userId}/ban")
    public ResponseEntity<UserResponse> ban(
            @PathVariable UUID userId,
            @AuthenticationPrincipal Jwt jwt) {
        User user = userService.banUser(userId, jwt);
        return ResponseEntity.ok(userService.toResponse(user));
    }

    @PatchMapping("/{userId}/unban")
    public ResponseEntity<UserResponse> unban(
            @PathVariable UUID userId,
            @AuthenticationPrincipal Jwt jwt) {
        User user = userService.unbanUser(userId, jwt);
        return ResponseEntity.ok(userService.toResponse(user));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID userId) {
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(userService.toResponse(user));
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String username) {
        Username u = new Username(username);
        User user = userService.getUserByUser(u);
        return ResponseEntity.ok(userService.toResponse(user));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId, @AuthenticationPrincipal Jwt jwt) {
        if (!userService.isAdmin(jwt)) {
            throw new InvalidValueException("Only admins can delete users");
        }
        userService.getUserById(userId);
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

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
                user.getBanned(),
                user.getCreatedAt(),
                user.getUpdatedAt());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/update")
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
                user.getBanned(),
                user.getCreatedAt(),
                user.getUpdatedAt());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}
