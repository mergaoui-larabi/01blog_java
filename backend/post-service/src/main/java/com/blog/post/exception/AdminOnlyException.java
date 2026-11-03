package com.blog.post.exception;

public class AdminOnlyException extends RuntimeException {
    public AdminOnlyException(String message) {
        super(message);
    }
}
