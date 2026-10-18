package com.blog.post.exception;

public class UserNotFollowingException extends RuntimeException {
    public UserNotFollowingException(String message) {
        super(message);
    }
}
