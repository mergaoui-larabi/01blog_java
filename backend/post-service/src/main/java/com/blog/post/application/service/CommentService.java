package com.blog.post.application.service;

import com.blog.post.application.dto.request.CommentCreateRequest;
import com.blog.post.domain.model.Comment;
import com.blog.post.repository.CommentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CommentService {

    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Transactional
    public Comment createComment(CommentCreateRequest request) {
        Comment comment = new Comment(
                request.postId(),
                request.authorId(),
                request.content(),
                request.parentCommentId());
        return commentRepository.save(comment);
    }

    @Transactional(readOnly = true)
    public Comment getCommentById(UUID id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
    }
}
