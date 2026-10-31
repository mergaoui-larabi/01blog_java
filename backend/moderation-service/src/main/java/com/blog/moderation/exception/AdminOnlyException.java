package com.blog.moderation.exception;

public class AdminOnlyException extends RuntimeException {

    public AdminOnlyException() {
        super("Administrator privileges are required");
    }
}
