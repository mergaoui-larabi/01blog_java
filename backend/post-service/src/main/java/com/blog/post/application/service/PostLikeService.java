package com.blog.post.application.service;

import com.blog.post.application.dto.request.PostLikeCreateRequest;
import com.blog.post.domain.model.PostLike;
import com.blog.post.repository.PostLikeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;

    public PostLikeService(PostLikeRepository postLikeRepository) {
        this.postLikeRepository = postLikeRepository;
    }

    @Transactional
    public PostLike createPostLike(PostLikeCreateRequest request) {
        PostLike postLike = new PostLike(request.postId(), request.userId());
        return postLikeRepository.save(postLike);
    }

    @Transactional(readOnly = true)
    public PostLike getPostLikeById(UUID id) {
        return postLikeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PostLike not found"));
    }
}
