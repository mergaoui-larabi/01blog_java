package com.blog.post.controller;

import com.blog.post.domain.valueobject.AuthorId;
import com.blog.post.domain.valueobject.PostId;
import com.blog.post.domain.valueobject.UserId;
import com.blog.post.service.CommentService;
import com.blog.post.service.LikeService;
import com.blog.post.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;
    private final LikeService likeService;
    private final CommentService commentService;

    public PostController(PostService postService, LikeService likeService, CommentService commentService) {
        this.postService = postService;
        this.likeService = likeService;
        this.commentService = commentService;
    }

    @GetMapping("/feed")
    public ResponseEntity<?> getFeed(
            @AuthenticationPrincipal(expression = "jwt") Map<String, Object> claims) {
        String subject = (String) claims.get("sub");
        if (subject == null) {
            return ResponseEntity.badRequest().body("Missing subject in JWT");
        }
        AuthorId authorId = new AuthorId(subject);

        // In a full implementation this would come from the user service's follow graph.
        // For now the authenticated user sees their own posts plus the global feed.
        List<com.blog.post.domain.entity.Post> posts = postService.getFeed(authorId, List.of());

        return ResponseEntity.ok(toFeedResponse(posts));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<?> getPost(@PathVariable String postId) {
        PostId id = new PostId(postId);
        com.blog.post.domain.entity.Post post = postService.getPostById(id);

        return ResponseEntity.ok(toPostResponse(post));
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<?> getComments(
            @PathVariable String postId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size) {
        PostId id = new PostId(postId);
        com.blog.post.domain.entity.Post post = postService.getPostById(id);

        List<com.blog.post.domain.entity.Comment> comments = commentService.getCommentsByPost(id);

        if (cursor != null && !cursor.isBlank()) {
            comments = comments.stream()
                    .filter(c -> c.getId().compareTo(Long.parseLong(cursor)) > 0)
                    .limit(size)
                    .toList();
        } else {
            comments = comments.stream().limit(size).toList();
        }

        return ResponseEntity.ok(toCommentsResponse(comments));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyPosts(
            @AuthenticationPrincipal(expression = "jwt") Map<String, Object> claims) {
        String subject = (String) claims.get("sub");
        if (subject == null) {
            return ResponseEntity.badRequest().body("Missing subject in JWT");
        }
        AuthorId authorId = new AuthorId(subject);

        List<com.blog.post.domain.entity.Post> posts = postService.getPostsByAuthor(authorId);

        return ResponseEntity.ok(toFeedResponse(posts));
    }

    @PostMapping
    public ResponseEntity<?> createPost(
            @AuthenticationPrincipal(expression = "jwt") Map<String, Object> claims,
            @Valid @RequestBody CreatePostRequest request) {
        String subject = (String) claims.get("sub");
        if (subject == null) {
            return ResponseEntity.badRequest().body("Missing subject in JWT");
        }
        AuthorId authorId = new AuthorId(subject);

        com.blog.post.domain.entity.Post post = new com.blog.post.domain.entity.Post(
                new com.blog.post.domain.valueobject.PostId(UUID.randomUUID().toString()),
                authorId,
                new com.blog.post.domain.valueobject.PostContent(request.content()),
                request.mediaId() != null ? new com.blog.post.domain.valueobject.MediaId(request.mediaId()) : null,
                new com.blog.post.domain.valueobject.LikesCount(0L),
                new com.blog.post.domain.valueobject.CommentsCount(0L),
                new com.blog.post.domain.valueobject.Visibility(false)
        );

        com.blog.post.domain.entity.Post saved = postService.getPostRepository().save(post);

        return ResponseEntity.status(201).body(toPostResponse(saved));
    }

    @PostMapping("/{postId}/likes")
    public ResponseEntity<?> likePost(
            @AuthenticationPrincipal(expression = "jwt") Map<String, Object> claims,
            @PathVariable String postId) {
        String subject = (String) claims.get("sub");
        if (subject == null) {
            return ResponseEntity.badRequest().body("Missing subject in JWT");
        }
        UserId userId = new UserId(subject);
        PostId id = new PostId(postId);

        com.blog.post.domain.entity.Like like = likeService.like(userId, id);

        return ResponseEntity.ok(Map.of(
                "postId", postId,
                "liked", true,
                "likesCount", likeService.getLikesCount(id)
        ));
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<?> addComment(
            @AuthenticationPrincipal(expression = "jwt") Map<String, Object> claims,
            @PathVariable String postId,
            @Valid @RequestBody CreateCommentRequest request) {
        String subject = (String) claims.get("sub");
        if (subject == null) {
            return ResponseEntity.badRequest().body("Missing subject in JWT");
        }
        UserId userId = new UserId(subject);
        PostId id = new PostId(postId);

        com.blog.post.domain.entity.Comment comment = commentService.addComment(userId, id, new com.blog.post.domain.valueobject.CommentContent(request.content()));

        return ResponseEntity.status(201).body(toCommentResponse(comment));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<?> replacePost(
            @AuthenticationPrincipal(expression = "jwt") Map<String, Object> claims,
            @PathVariable String postId,
            @Valid @RequestBody ReplacePostRequest request) {
        String subject = (String) claims.get("sub");
        if (subject == null) {
            return ResponseEntity.badRequest().body("Missing subject in JWT");
        }
        AuthorId authorId = new AuthorId(subject);
        PostId id = new PostId(postId);

        if (!postService.isPostOwnedBy(id, authorId)) {
            return ResponseEntity.status(403).body("Not authorized to update this post");
        }

        com.blog.post.domain.entity.Post post = postService.getPostById(id);
        post.updateContent(new com.blog.post.domain.valueobject.PostContent(request.content()));
        post.updateMediaId(request.mediaId() != null ? new com.blog.post.domain.valueobject.MediaId(request.mediaId()) : null);

        com.blog.post.domain.entity.Post saved = postService.getPostRepository().save(post);

        return ResponseEntity.ok(toPostResponse(saved));
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<?> patchPost(
            @AuthenticationPrincipal(expression = "jwt") Map<String, Object> claims,
            @PathVariable String postId,
            @Valid @RequestBody Map<String, Object> patch) {
        String subject = (String) claims.get("sub");
        if (subject == null) {
            return ResponseEntity.badRequest().body("Missing subject in JWT");
        }
        AuthorId authorId = new AuthorId(subject);
        PostId id = new PostId(postId);

        if (!postService.isPostOwnedBy(id, authorId)) {
            return ResponseEntity.status(403).body("Not authorized to update this post");
        }

        com.blog.post.domain.entity.Post post = postService.getPostById(id);

        if (patch.containsKey("content")) {
            post.updateContent(new com.blog.post.domain.valueobject.PostContent((String) patch.get("content")));
        }
        if (patch.containsKey("mediaId")) {
            Object mediaId = patch.get("mediaId");
            if (mediaId == null || mediaId.toString().isEmpty()) {
                post.updateMediaId(null);
            } else {
                post.updateMediaId(new com.blog.post.domain.valueobject.MediaId(mediaId.toString()));
            }
        }
        if (patch.containsKey("hidden")) {
            Boolean hidden = (Boolean) patch.get("hidden");
            if (hidden != null) {
                post.hide();
            } else {
                post.unhide();
            }
        }

        com.blog.post.domain.entity.Post saved = postService.getPostRepository().save(post);

        return ResponseEntity.ok(toPostResponse(saved));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(
            @AuthenticationPrincipal(expression = "jwt") Map<String, Object> claims,
            @PathVariable String postId) {
        String subject = (String) claims.get("sub");
        if (subject == null) {
            return ResponseEntity.badRequest().body("Missing subject in JWT");
        }
        AuthorId authorId = new AuthorId(subject);
        PostId id = new PostId(postId);

        if (!postService.isPostOwnedBy(id, authorId)) {
            return ResponseEntity.status(403).body("Not authorized to delete this post");
        }

        postService.getPostRepository().deleteById(UUID.fromString(postId));

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{postId}/likes")
    public ResponseEntity<?> unlikePost(
            @AuthenticationPrincipal(expression = "jwt") Map<String, Object> claims,
            @PathVariable String postId) {
        String subject = (String) claims.get("sub");
        if (subject == null) {
            return ResponseEntity.badRequest().body("Missing subject in JWT");
        }
        UserId userId = new UserId(subject);
        PostId id = new PostId(postId);

        likeService.unlike(userId, id);

        return ResponseEntity.ok(Map.of(
                "postId", postId,
                "liked", false,
                "likesCount", likeService.getLikesCount(id)
        ));
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
        UserId userId = new UserId(subject);

        commentService.deleteComment(userId, commentId);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<?> replaceComment(
            @AuthenticationPrincipal(expression = "jwt") Map<String, Object> claims,
            @PathVariable String postId,
            @PathVariable Long commentId,
            @Valid @RequestBody ReplaceCommentRequest request) {
        String subject = (String) claims.get("sub");
        if (subject == null) {
            return ResponseEntity.badRequest().body("Missing subject in JWT");
        }
        UserId userId = new UserId(subject);

        com.blog.post.domain.entity.Comment comment = commentService.updateCommentContent(
                userId, commentId, new com.blog.post.domain.valueobject.CommentContent(request.content()));

        return ResponseEntity.ok(toCommentResponse(comment));
    }

    private Map<String, Object> toPostResponse(com.blog.post.domain.entity.Post post) {
        return Map.of(
                "id", post.getId(),
                "authorId", post.getAuthorId().value().toString(),
                "content", post.getContent(),
                "mediaId", post.getMediaId() != null ? post.getMediaId().value() : null,
                "likesCount", post.getLikesCount(),
                "commentsCount", post.getCommentsCount(),
                "hidden", post.getHidden(),
                "createdAt", post.getCreatedAt().toString(),
                "updatedAt", post.getUpdatedAt().toString()
        );
    }

    private Map<String, Object> toFeedResponse(List<com.blog.post.domain.entity.Post> posts) {
        return Map.of("posts", posts.stream().map(this::toPostResponse).toList());
    }

    private Map<String, Object> toCommentsResponse(List<com.blog.post.domain.entity.Comment> comments) {        return Map.of("comments", comments.stream().map(PostController::toCommentResponse).toList());
    }

    public static class CreatePostRequest {
        private String content;
        private String mediaId;

        public CreatePostRequest() {
        }

        public CreatePostRequest(String content, String mediaId) {
            this.content = content;
            this.mediaId = mediaId;
        }

        public String content() {
            return content;
        }

        public String mediaId() {
            return mediaId;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public void setMediaId(String mediaId) {
            this.mediaId = mediaId;
        }
    }

    public static class CreateCommentRequest {
        private String content;

        public CreateCommentRequest() {
        }

        public CreateCommentRequest(String content) {
            this.content = content;
        }

        public String content() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    public static class ReplacePostRequest {
        private String content;
        private String mediaId;

        public ReplacePostRequest() {
        }

        public ReplacePostRequest(String content, String mediaId) {
            this.content = content;
            this.mediaId = mediaId;
        }

        public String content() {
            return content;
        }

        public String mediaId() {
            return mediaId;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public void setMediaId(String mediaId) {
            this.mediaId = mediaId;
        }
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

    static Map<String, Object> toCommentResponse(com.blog.post.domain.entity.Comment comment) {
        return Map.of(
                "id", comment.getId(),
                "postId", comment.getPostId().value(),
                "authorId", comment.getAuthorId().value().toString(),
                "content", comment.getContent(),
                "createdAt", comment.getCreatedAt().toString(),
                "updatedAt", comment.getUpdatedAt().toString()
        );
    }
}
