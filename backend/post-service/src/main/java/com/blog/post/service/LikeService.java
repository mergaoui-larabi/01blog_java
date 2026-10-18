package com.blog.post.service;

import com.blog.post.domain.entity.Like;
import com.blog.post.domain.valueobject.PostId;
import com.blog.post.domain.valueobject.UserId;
import com.blog.post.exception.LikeNotFoundException;
import com.blog.post.exception.UnlikeNotAllowedException;
import com.blog.post.repository.LikeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostService postService;

    public LikeService(LikeRepository likeRepository, PostService postService) {
        this.likeRepository = likeRepository;
        this.postService = postService;
    }

    @Transactional
    public com.blog.post.domain.entity.Like like(UserId userId, PostId postId) {
        postService.getPostById(postId);

        if (likeRepository.existsByPostIdAndUserId(postId, userId)) {
            return likeRepository.findByPostIdAndUserId(postId, userId).orElse(null);
        }

        return likeRepository.save(new com.blog.post.domain.entity.Like(userId, postId));
    }

    @Transactional
    public void unlike(UserId userId, PostId postId) {
        com.blog.post.domain.entity.Like existing = likeRepository.findByPostIdAndUserId(postId, userId)
                .orElseThrow(() -> new LikeNotFoundException("Like not found"));

        likeRepository.delete(existing);
    }

    @Transactional(readOnly = true)
    public long getLikesCount(PostId postId) {
        return likeRepository.countByPostId(postId);
    }

    @Transactional(readOnly = true)
    public boolean hasUserLiked(UserId userId, PostId postId) {
        return likeRepository.existsByPostIdAndUserId(postId, userId);
    }
}
