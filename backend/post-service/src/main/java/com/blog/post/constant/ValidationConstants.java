package com.blog.post.constant;

import java.util.regex.Pattern;

public final class ValidationConstants {
    private ValidationConstants() {
    }

    // comment
    public static final int COMMENT_CONTENT_MAX_LENGTH = 1024 * 1024;
    //post
    public static final int POST_CONTENT_MAX_LENGTH = 10 * 1024 * 1024;

    // Username
    public static final int USERNAME_MIN_LENGTH = 3;
    public static final int USERNAME_MAX_LENGTH = 30;
    public static final Pattern USERNAME_REGEX_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_-]*$");

    // Password
    public static final int PASSWORD_MIN_LENGTH = 8;
    public static final int PASSWORD_MAX_LENGTH = 128;
    public static final Pattern PASSWORD_REGEX_PATTERN = Pattern
            .compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d])\\S+$");

    // Age
    public static final Short AGE_MAX_NUMBER = 100;
    public static final Short AGE_MIN_NUMBER = 12;
}
