package com.blog.post.service;

import com.blog.post.domain.entity.Comment;
import com.blog.post.domain.valueobject.AuthorId;
import com.blog.post.domain.valueobject.CommentContent;
import com.blog.post.domain.valueobject.PostId;
import com.blog.post.domain.valueobject.UserId;
import com.blog.post.exception.CommentNotFoundException;
import com.blog.post.exception.CommentNotOwnedException;
import com.blog.post.exception.PostNotFoundException;
import com.blog.post.repository.CommentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostService postService;

    public CommentService(CommentRepository commentRepository, PostService postService) {
        this.commentRepository = commentRepository;
        this.postService = postService;
    }

    @Transactional
    public Comment addComment(UserId userId, PostId postId, CommentContent content) {
        postService.getPostById(postId);

        Comment comment = new Comment(postId, new AuthorId(userId.value().toString()), content);
        return commentRepository.save(comment);
    }

    @Transactional(readOnly = true)
    public List<Comment> getCommentsByPost(PostId postId) {
        return commentRepository.findCommentsByPostId(postId);
    }

    @Transactional
    public Comment updateCommentContent(UserId userId, Long commentId, CommentContent content) {
        PostId postId = commentRepository.findPostIdById(commentId)
                .orElseThrow(() -> new PostNotFoundException("Comment not found"));

        Comment comment = commentRepository.findByPostIdAndId(postId, commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found"));

        if (!comment.getAuthorId().value().equals(userId.value())) {
            throw new CommentNotOwnedException("Comment not owned by user");
        }

        comment.updateContent(content);
        return commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(UserId userId, Long commentId) {
        PostId postId = commentRepository.findPostIdById(commentId)
                .orElseThrow(() -> new PostNotFoundException("Comment not found"));

        Comment comment = commentRepository.findByPostIdAndId(postId, commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found"));

        if (!comment.getAuthorId().value().equals(userId.value())) {
            throw new CommentNotOwnedException("Comment not owned by user");
        }

        commentRepository.delete(comment);
    }
}
