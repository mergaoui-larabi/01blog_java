package com.blog.post.service;

import com.blog.post.domain.valueobject.PostId;
import com.blog.post.exception.LikeNotFoundException;
import com.blog.post.repository.LikeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class LikeService {

    private final LikeRepository likeRepository;

    public LikeService(LikeRepository likeRepository) {
        this.likeRepository = likeRepository;
    }

    @Transactional
    public void unlike(PostId postId, UUID userId) {
        com.blog.post.domain.entity.Like like = likeRepository.findByPostIdAndUserId(postId.value(), userId)
                .orElseThrow(() -> new LikeNotFoundException("Like not found: " + postId));
        likeRepository.delete(like);
    }
}
