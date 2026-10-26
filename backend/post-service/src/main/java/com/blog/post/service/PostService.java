package com.blog.post.service;

import com.blog.post.application.dto.post.PageResponse;
import com.blog.post.application.dto.post.PostResponse;
import com.blog.post.application.dto.request.CreatePostRequest;
import com.blog.post.application.dto.request.ReplacePostRequest;
import com.blog.post.application.dto.request.UpdatePostRequest;
import com.blog.post.client.MediaServiceClient;
import com.blog.post.client.UserServiceClient;
import com.blog.post.domain.entity.Comment;
import com.blog.post.domain.entity.Like;
import com.blog.post.domain.entity.Post;
import com.blog.post.domain.model.CommentModel;
import com.blog.post.domain.valueobject.CommentContent;
import com.blog.post.domain.valueobject.PostContent;
import com.blog.post.exception.CommentNotFoundException;
import com.blog.post.exception.CommentNotOwnedException;
import com.blog.post.exception.InvalidValueException;
import com.blog.post.exception.PostNotFoundException;
import com.blog.post.exception.PostNotOwnedException;
import com.blog.post.exception.UnlikeNotAllowedException;
import com.blog.post.repository.CommentRepository;
import com.blog.post.repository.LikeRepository;
import com.blog.post.repository.PostRepository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final UserServiceClient userServiceClient;
    private final MediaServiceClient mediaServiceClient;

    public PostService(
            PostRepository postRepository,
            CommentRepository commentRepository,
            LikeRepository likeRepository,
            UserServiceClient userServiceClient,
            MediaServiceClient mediaServiceClient) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
        this.userServiceClient = userServiceClient;
        this.mediaServiceClient = mediaServiceClient;
    }

    // -------------------------------------------------------------------------
    // Feed & Post Queries
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public PostResponse fetchPost(UUID postId, UUID currentUserId) {
        Post post = requirePost(postId);
        boolean liked = currentUserId != null && likeRepository.existsByPostIdAndUserId(postId, currentUserId);
        return toPostResponse(post, currentUserId, liked);
    }

    @Transactional(readOnly = true)
    public PageResponse<PostResponse> fetchFeed(UUID currentUserId, UUID cursor, int size) {
        Objects.requireNonNull(currentUserId, "Authenticated user ID is required to fetch feed");

        List<UUID> followedUserIds = userServiceClient.getFollowedUserIds(currentUserId);
        Set<UUID> authorIds = new HashSet<>(followedUserIds);
        authorIds.add(currentUserId);

        Pageable pageable = PageRequest.ofSize(size + 1);
        List<Post> posts;

        if (cursor == null) {
            posts = postRepository.findFeedFirstPage(authorIds, pageable);
        } else {
            Post cursorPost = postRepository.findById(cursor).orElse(null);
            Instant cursorCreatedAt = cursorPost != null ? cursorPost.getCreatedAt() : Instant.now();
            posts = postRepository.findFeedWithCursor(authorIds, cursor, cursorCreatedAt, pageable);
        }

        return buildPageResponse(posts, size, currentUserId);
    }

    @Transactional(readOnly = true)
    public PageResponse<PostResponse> fetchMyPosts(UUID currentUserId, UUID cursor, int size) {
        Objects.requireNonNull(currentUserId, "Authenticated user ID is required to fetch posts");

        Pageable pageable = PageRequest.ofSize(size + 1);
        List<Post> posts;

        if (cursor == null) {
            posts = postRepository.findMyPostsFirstPage(currentUserId, pageable);
        } else {
            Post cursorPost = postRepository.findById(cursor).orElse(null);
            Instant cursorCreatedAt = cursorPost != null ? cursorPost.getCreatedAt() : Instant.now();
            posts = postRepository.findMyPostsWithCursor(currentUserId, cursor, cursorCreatedAt, pageable);
        }

        return buildPageResponse(posts, size, currentUserId);
    }

    // -------------------------------------------------------------------------
    // Post Mutations
    // -------------------------------------------------------------------------

    @Transactional
    public PostResponse createPost(CreatePostRequest request, UUID userId, String authHeader) {
        List<UUID> mediaUuids = parseAndDeduplicateMediaIds(request.mediaIds());
        if (!mediaUuids.isEmpty()) {
            mediaServiceClient.validateAllMediaOwnership(mediaUuids, authHeader);
        }

        Post post = new Post(
                userId,
                new PostContent(request.content()),
                mediaUuids);

        post = postRepository.save(post);
        return toPostResponse(post, userId, false);
    }

    @Transactional
    public PostResponse replacePost(UUID postId, ReplacePostRequest request, UUID userId, String authHeader) {
        Post post = requireOwnPost(postId, userId);

        List<UUID> mediaUuids = parseAndDeduplicateMediaIds(request.mediaIds());
        if (!mediaUuids.isEmpty()) {
            mediaServiceClient.validateAllMediaOwnership(mediaUuids, authHeader);
        }

        post.setContent(new PostContent(request.content()));
        post.setMediaIds(mediaUuids);
        post.touch();
        post = postRepository.save(post);

        boolean liked = likeRepository.existsByPostIdAndUserId(postId, userId);
        return toPostResponse(post, userId, liked);
    }

    @Transactional
    public PostResponse updatePost(UUID postId, UpdatePostRequest request, UUID userId, String authHeader) {
        Post post = requireOwnPost(postId, userId);

        if (request.content() != null) {
            post.setContent(new PostContent(request.content()));
        }

        if (request.mediaIds() != null) {
            List<UUID> mediaUuids = parseAndDeduplicateMediaIds(request.mediaIds());
            if (!mediaUuids.isEmpty()) {
                mediaServiceClient.validateAllMediaOwnership(mediaUuids, authHeader);
            }
            post.setMediaIds(mediaUuids);
        }

        if (request.hidden() != null) {
            post.setHidden(request.hidden());
        }

        post.touch();
        post = postRepository.save(post);

        boolean liked = likeRepository.existsByPostIdAndUserId(postId, userId);
        return toPostResponse(post, userId, liked);
    }

    @Transactional
    public void deletePost(UUID postId, UUID userId, boolean isAdmin) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found: " + postId));
        if (!post.getAuthorId().equals(userId) && !isAdmin) {
            throw new PostNotOwnedException("Post not owned by current user: " + postId);
        }
        postRepository.delete(post);
    }

    // -------------------------------------------------------------------------
    // Likes
    // -------------------------------------------------------------------------

    @Transactional
    public void likePost(UUID postId, UUID userId) {
        Post post = requirePost(postId);
        if (likeRepository.existsByPostIdAndUserId(postId, userId)) {
            throw new UnlikeNotAllowedException("User already liked this post");
        }
        Like like = new Like(postId, userId);
        likeRepository.save(like);
        post.incrementLikesCount();
        postRepository.save(post);
    }

    @Transactional
    public void unlikePost(UUID postId, UUID userId) {
        Post post = requirePost(postId);
        Like like = likeRepository.findByPostIdAndUserId(postId, userId)
                .orElseThrow(() -> new UnlikeNotAllowedException("User has not liked this post"));
        likeRepository.delete(like);
        post.decrementLikesCount();
        postRepository.save(post);
    }

    // -------------------------------------------------------------------------
    // Comments
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public PageResponse<CommentModel> fetchComments(UUID postId, UUID cursor, int size) {
        requirePost(postId);
        Pageable pageable = PageRequest.ofSize(size + 1);

        Instant cursorTime = null;
        if (cursor != null) {
            cursorTime = commentRepository.findById(cursor)
                    .map(Comment::getCreatedAt)
                    .orElse(null);
        }

        List<Comment> comments = commentRepository.findCommentsByPostIdWithCursor(
                postId, cursor, cursorTime, pageable);

        boolean hasNext = comments.size() > size;
        List<Comment> pageContent = hasNext ? comments.subList(0, size) : comments;
        String nextCursor = hasNext && !pageContent.isEmpty()
                ? pageContent.get(pageContent.size() - 1).getId().toString()
                : null;

        List<CommentModel> models = pageContent.stream()
                .map(CommentModel::from)
                .toList();

        return new PageResponse<>(models, nextCursor, hasNext);
    }

    @Transactional
    public CommentModel createComment(UUID postId, String content, UUID userId) {
        Post post = requirePost(postId);
        Comment comment = new Comment(
                postId,
                userId,
                new CommentContent(content));

        comment = commentRepository.save(comment);
        post.incrementCommentsCount();
        postRepository.save(post);
        return CommentModel.from(comment);
    }

    @Transactional
    public CommentModel replaceComment(UUID postId, UUID commentId, String content, UUID userId) {
        Comment comment = requireComment(postId, commentId);
        ensureCommentOwnedByCurrentUser(comment, userId);
        comment.setContent(new CommentContent(content));
        comment.setUpdatedAt(Instant.now());
        comment = commentRepository.save(comment);
        return CommentModel.from(comment);
    }

    @Transactional
    public CommentModel updateComment(UUID postId, UUID commentId, String content, UUID userId) {
        Comment comment = requireComment(postId, commentId);
        ensureCommentOwnedByCurrentUser(comment, userId);
        if (content != null) {
            comment.setContent(new CommentContent(content));
        }
        comment.setUpdatedAt(Instant.now());
        comment = commentRepository.save(comment);
        return CommentModel.from(comment);
    }

    @Transactional
    public void deleteComment(UUID postId, UUID commentId, UUID userId, boolean isAdmin) {
        Comment comment = requireComment(postId, commentId);
        if (!comment.getAuthorId().equals(userId) && !isAdmin) {
            throw new CommentNotOwnedException("Comment not owned by current user: " + commentId);
        }
        commentRepository.delete(comment);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found: " + postId));
        post.decrementCommentsCount();
        postRepository.save(post);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Post requirePost(UUID postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found: " + postId));
    }

    private Post requireOwnPost(UUID postId, UUID userId) {
        return postRepository.findByIdAndAuthorId(postId, userId)
                .orElseThrow(() -> new PostNotOwnedException("Post not owned by current user: " + postId));
    }

    private Comment requireComment(UUID postId, UUID commentId) {
        return commentRepository.findById(commentId)
                .filter(c -> c.getPostId().equals(postId))
                .orElseThrow(() -> new CommentNotFoundException("Comment not found: " + commentId));
    }

    private void ensureCommentOwnedByCurrentUser(Comment comment, UUID userId) {
        if (!userId.equals(comment.getAuthorId())) {
            throw new CommentNotOwnedException("Comment not owned by current user: " + comment.getId());
        }
    }

    private PageResponse<PostResponse> buildPageResponse(List<Post> posts, int size, UUID currentUserId) {
        boolean hasNext = posts.size() > size;
        List<Post> contentPosts = hasNext ? posts.subList(0, size) : posts;
        String nextCursor = hasNext && !contentPosts.isEmpty()
                ? contentPosts.get(contentPosts.size() - 1).getId().toString()
                : null;

        List<UUID> postIds = contentPosts.stream().map(Post::getId).toList();
        Set<UUID> likedPostIds = (currentUserId != null && !postIds.isEmpty())
                ? likeRepository.findLikedPostIdsByUserIdAndPostIdIn(currentUserId, postIds)
                : Collections.emptySet();

        List<PostResponse> responses = contentPosts.stream()
                .map(post -> toPostResponse(post, currentUserId, likedPostIds.contains(post.getId())))
                .toList();

        return new PageResponse<>(responses, nextCursor, hasNext);
    }

    private List<UUID> parseAndDeduplicateMediaIds(List<String> mediaIdStrings) {
        if (mediaIdStrings == null || mediaIdStrings.isEmpty()) {
            return Collections.emptyList();
        }
        Set<UUID> uniqueUuids = new LinkedHashSet<>();
        for (String idStr : mediaIdStrings) {
            if (idStr != null && !idStr.isBlank()) {
                try {
                    uniqueUuids.add(UUID.fromString(idStr.trim()));
                } catch (IllegalArgumentException e) {
                    throw new InvalidValueException("Invalid UUID format for media ID: " + idStr);
                }
            }
        }
        return new ArrayList<>(uniqueUuids);
    }

    private PostResponse toPostResponse(Post post, UUID currentUserId, boolean likedByCurrentUser) {
        boolean ownedByCurrentUser = currentUserId != null && post.getAuthorId().equals(currentUserId);

        List<String> mediaIds = post.getMediaIds() != null
                ? post.getMediaIds().stream().map(UUID::toString).toList()
                : Collections.emptyList();

        return new PostResponse(
                post.getId().toString(),
                post.getAuthorId().toString(),
                post.getContent().value(),
                mediaIds,
                post.getLikesCount().value(),
                post.getCommentsCount().value(),
                likedByCurrentUser,
                ownedByCurrentUser,
                post.getHidden(),
                post.getCreatedAt(),
                post.getUpdatedAt());
    }
}
