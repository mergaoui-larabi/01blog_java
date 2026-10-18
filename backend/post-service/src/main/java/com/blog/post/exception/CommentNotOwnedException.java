package com.blog.post.exception;

public class CommentNotOwnedException extends RuntimeException {
    public CommentNotOwnedException(String message) {
        super(message);
    }
}
