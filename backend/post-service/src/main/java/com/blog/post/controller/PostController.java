package com.blog.post.controller;

import com.blog.post.application.dto.post.PageResponse;
import com.blog.post.application.dto.post.PostResponse;
import com.blog.post.application.dto.request.CreateCommentRequest;
import com.blog.post.application.dto.request.CreatePostRequest;
import com.blog.post.application.dto.request.ReplacePostRequest;
import com.blog.post.application.dto.request.UpdatePostRequest;
import com.blog.post.constant.SecurityConstants;
import com.blog.post.domain.model.CommentModel;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/posts")
@Validated
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    // -------------------------------------------------------------------------
    // Posts
    // -------------------------------------------------------------------------

    @GetMapping("/feed")
    public ResponseEntity<PageResponse<PostResponse>> fetchFeed(
            @RequestParam(required = false) UUID cursor,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(postService.fetchFeed(userId(jwt), cursor, size));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> fetchPost(
            @PathVariable UUID postId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID currentUserId = jwt != null ? userId(jwt) : null;
        return ResponseEntity.ok(postService.fetchPost(postId, currentUserId));
    }

    @GetMapping("/me")
    public ResponseEntity<PageResponse<PostResponse>> fetchMyPosts(
            @RequestParam(required = false) UUID cursor,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(postService.fetchMyPosts(userId(jwt), cursor, size));
    }

    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @Valid @RequestBody CreatePostRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(postService.createPost(request, userId(jwt), authHeader(jwt)));
    }

    @PostMapping("/{postId}/likes")
    public ResponseEntity<Void> likePost(
            @PathVariable UUID postId,
            @AuthenticationPrincipal Jwt jwt) {
        postService.likePost(postId, userId(jwt));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{postId}")
    public ResponseEntity<PostResponse> replacePost(
            @PathVariable UUID postId,
            @Valid @RequestBody ReplacePostRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(postService.replacePost(postId, request, userId(jwt), authHeader(jwt)));
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable UUID postId,
            @Valid @RequestBody UpdatePostRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(postService.updatePost(postId, request, userId(jwt), authHeader(jwt)));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable UUID postId,
            @AuthenticationPrincipal Jwt jwt) {
        boolean isAdmin = isAdmin(jwt);
        postService.deletePost(postId, userId(jwt), isAdmin);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{postId}/likes")
    public ResponseEntity<Void> unlikePost(
            @PathVariable UUID postId,
            @AuthenticationPrincipal Jwt jwt) {
        postService.unlikePost(postId, userId(jwt));
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // Comments
    // -------------------------------------------------------------------------

    @GetMapping("/{postId}/comments")
    public ResponseEntity<PageResponse<CommentModel>> fetchComments(
            @PathVariable UUID postId,
            @RequestParam(required = false) UUID cursor,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(postService.fetchComments(postId, cursor, size));
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentModel> createComment(
            @PathVariable UUID postId,
            @Valid @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(postService.createComment(postId, request.content(), userId(jwt)));
    }

    @PutMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<CommentModel> replaceComment(
            @PathVariable UUID postId,
            @PathVariable UUID commentId,
            @Valid @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(postService.replaceComment(postId, commentId, request.content(), userId(jwt)));
    }

    @PatchMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<CommentModel> updateComment(
            @PathVariable UUID postId,
            @PathVariable UUID commentId,
            @Valid @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(postService.updateComment(postId, commentId, request.content(), userId(jwt)));
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable UUID postId,
            @PathVariable UUID commentId,
            @AuthenticationPrincipal Jwt jwt) {
        boolean isAdmin = isAdmin(jwt);
        postService.deleteComment(postId, commentId, userId(jwt), isAdmin);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("ping")
    public String ping() {
        return "pong";
    }

    private UUID userId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }

    private String authHeader(Jwt jwt) {
        return jwt != null ? "Bearer " + jwt.getTokenValue() : null;
    }

    private boolean isAdmin(Jwt jwt) {
        return SecurityConstants.ADMIN_ROLE.equals(jwt.getClaimAsString(SecurityConstants.ROLE_CLAIM));
    }
}
