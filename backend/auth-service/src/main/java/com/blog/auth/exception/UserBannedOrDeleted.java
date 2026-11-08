package com.blog.auth.exception;

public class UserBannedOrDeleted extends RuntimeException {
    public UserBannedOrDeleted(String message) {
        super(message);
    }
}