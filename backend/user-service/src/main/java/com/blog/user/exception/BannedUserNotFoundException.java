package com.blog.user.exception;

public class BannedUserNotFoundException extends RuntimeException {

    public BannedUserNotFoundException(String message) {
        super(message);
    }
}
