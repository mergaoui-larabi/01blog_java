package com.blog.post.controller;

import com.blog.post.domain.valueobject.UserId;
import com.blog.post.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/posts")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PutMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<?> replaceComment(
            @AuthenticationPrincipal(expression = "jwt") Map<String, Object> claims,
            @PathVariable String postId,
            @PathVariable Long commentId,
            @RequestBody ReplaceCommentRequest request) {
        String subject = (String) claims.get("sub");
        if (subject == null) {
            return ResponseEntity.badRequest().body("Missing subject in JWT");
        }

        com.blog.post.domain.entity.Comment comment = commentService.updateCommentContent(
                new UserId(subject),
                commentId,
                new com.blog.post.domain.valueobject.CommentContent(request.content()));

        return ResponseEntity.ok(PostController.toCommentResponse(comment));
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<?> deleteComment(
            @AuthenticationPrincipal(expression = "jwt") Map<String, Object> claims,
            @PathVariable String postId,
            @PathVariable Long commentId) {
        String subject = (String) claims.get("sub");
        if (subject == null) {
            return ResponseEntity.badRequest().body("Missing subject in JWT");
        }

        commentService.deleteComment(
                new UserId(subject),
                commentId);

        return ResponseEntity.ok().build();
    }

    private static Map<String, Object> toCommentResponse(com.blog.post.domain.entity.Comment comment) {
        return Map.of(
                "id", comment.getId(),
                "postId", comment.getPostId().value(),
                "authorId", comment.getAuthorId().value(),
                "content", comment.getContent(),
                "createdAt", comment.getCreatedAt().toString(),
                "updatedAt", comment.getUpdatedAt().toString()
        );
    }

    public static class ReplaceCommentRequest {
        private String content;

        public ReplaceCommentRequest() {
        }

        public ReplaceCommentRequest(String content) {
            this.content = content;
        }

        public String content() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
