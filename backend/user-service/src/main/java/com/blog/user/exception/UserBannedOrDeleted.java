package com.blog.user.exception;

public class UserBannedOrDeleted extends RuntimeException {
    public UserBannedOrDeleted(String message) {
        super(message);
    }
}