package com.blog.post.controller;

import com.blog.post.application.dto.post.PageResponse;
import com.blog.post.application.dto.post.PostResponse;
import com.blog.post.application.dto.request.HidePostRequest;
import com.blog.post.constant.SecurityConstants;
import com.blog.post.exception.AdminOnlyException;
import com.blog.post.service.PostService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/posts/admin")
@Validated
public class AdminPostController {

    private final PostService postService;

    public AdminPostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<PostResponse>> searchPosts(
            @RequestParam(required = false) UUID postId,
            @RequestParam(required = false) UUID authorId,
            @RequestParam(required = false) UUID cursor,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @AuthenticationPrincipal Jwt jwt) {
        requireAdmin(jwt);
        return ResponseEntity.ok(postService.searchPosts(postId, authorId, cursor, size));
    }

    @PatchMapping("/{postId}/hidden")
    public ResponseEntity<PostResponse> setPostHidden(
            @PathVariable UUID postId,
            @Valid @RequestBody HidePostRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        requireAdmin(jwt);
        return ResponseEntity.ok(postService.setPostHidden(postId, request.hidden()));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable UUID postId,
            @AuthenticationPrincipal Jwt jwt) {
        requireAdmin(jwt);
        postService.deletePost(postId, userId(jwt), true);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("ping")
    public String ping() {
        return "pong";
    }

    private void requireAdmin(Jwt jwt) {
        if (jwt == null || !SecurityConstants.ADMIN_ROLE.equals(jwt.getClaimAsString(SecurityConstants.ROLE_CLAIM))) {
            throw new AdminOnlyException("Admin privileges are required");
        }
    }

    private UUID userId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
