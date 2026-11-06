package com.blog.post.service;

import com.blog.post.application.dto.UserCleanupResponse;
import com.blog.post.domain.entity.Comment;
import com.blog.post.domain.entity.Like;
import com.blog.post.domain.entity.Post;
import com.blog.post.repository.CommentRepository;
import com.blog.post.repository.LikeRepository;
import com.blog.post.repository.PostRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Removes the data Post Service owns for a deleted account. Posts and comments are anonymized rather
 * than deleted, likes are removed. Every step is driven by "does this row still reference the user",
 * so calling it again after a partial failure is a no-op.
 */
@Service
public class UserCleanupService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;

    public UserCleanupService(
            PostRepository postRepository,
            CommentRepository commentRepository,
            LikeRepository likeRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
    }

    @Transactional
    public UserCleanupResponse cleanup(UUID userId) {
        List<Like> likes = likeRepository.findByUserId(userId);
        if (!likes.isEmpty()) {
            List<UUID> likedPostIds = likes.stream().map(Like::getPostId).distinct().toList();
            List<Post> likedPosts = postRepository.findAllById(likedPostIds);
            // The (post_id, user_id) unique constraint means at most one like per post to remove.
            likedPosts.forEach(Post::decrementLikesCount);
            postRepository.saveAll(likedPosts);
            likeRepository.deleteAll(likes);
        }

        List<Post> posts = postRepository.findByAuthorId(userId);
        posts.forEach(Post::anonymizeAuthor);
        postRepository.saveAll(posts);

        List<Comment> comments = commentRepository.findByAuthorId(userId);
        comments.forEach(Comment::anonymizeAuthor);
        commentRepository.saveAll(comments);

        return new UserCleanupResponse(userId, posts.size(), comments.size(), likes.size());
    }
}
