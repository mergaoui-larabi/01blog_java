package com.blog.post.application.service;

import com.blog.post.application.dto.request.PostCreateRequest;
import com.blog.post.domain.model.Post;
import com.blog.post.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Transactional
    public Post createPost(PostCreateRequest request) {
        Post post = new Post(request.authorId(), request.content());
        return postRepository.save(post);
    }

    @Transactional(readOnly = true)
    public Post getPostById(UUID id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
    }
}
