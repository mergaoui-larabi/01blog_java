package com.blog.post.service;

import com.blog.post.domain.entity.Comment;
import com.blog.post.domain.entity.Post;
import java.util.UUID;
import com.blog.post.domain.valueobject.CommentsCount;
import com.blog.post.exception.CommentNotFoundException;
import com.blog.post.exception.CommentNotOwnedException;
import com.blog.post.repository.CommentRepository;
import com.blog.post.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    public CommentService(CommentRepository commentRepository,
            PostRepository postRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
    }

    @Transactional
    public void delete(UUID postId, UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
                .filter(c -> c.getPostId().equals(postId))
                .orElseThrow(() -> new CommentNotFoundException("Comment not found: " + commentId));
        commentRepository.delete(comment);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CommentNotFoundException("Post not found: " + postId));
        long count = commentRepository.countByPostId(postId);
        post.setCommentsCount(new CommentsCount(count));
        postRepository.save(post);
    }
}
