package com.blog.user.constant;

import java.util.regex.Pattern;

public final class ValidationConstants {
    private ValidationConstants() {
    }

    // Email
    public static final int EMAIL_MAX_LENGTH = 255;
    public static final Pattern EMAIL_REGEX_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    // Username
    public static final int USERNAME_MIN_LENGTH = 3;
    public static final int USERNAME_MAX_LENGTH = 30;
    public static final Pattern USERNAME_REGEX_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_-]*$");

    // Password
    public static final int PASSWORD_MIN_LENGTH = 8;
    public static final int PASSWORD_MAX_LENGTH = 128;
    public static final Pattern PASSWORD_REGEX_PATTERN = Pattern
            .compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d])\\S+$");

    //Age
    public static final Short AGE_MAX_NUMBER = 100;
    public static final Short AGE_MIN_NUMBER = 12;
}
