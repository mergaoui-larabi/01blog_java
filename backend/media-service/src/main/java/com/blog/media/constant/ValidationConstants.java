package com.blog.media.constant;

import java.util.regex.Pattern;

public final class ValidationConstants {
    private ValidationConstants() {
    }

    // Original filename
    public static final int ORIGINAL_FILENAME_MAX_LENGTH = 255;

    // Storage key
    public static final int STORAGE_KEY_MAX_LENGTH = 500;
    public static final Pattern STORAGE_KEY_REGEX_PATTERN = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9/._-]*$");

    // Detected content type
    public static final int CONTENT_TYPE_MAX_LENGTH = 100;
    public static final Pattern CONTENT_TYPE_REGEX_PATTERN = Pattern
            .compile("^[a-z0-9!#$&^_.+-]+/[a-z0-9!#$&^_.+-]+$");
    public static final java.util.Set<String> ALLOWED_CONTENT_TYPES = java.util.Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
            "image/avif",
            "video/mp4",
            "video/webm",
            "video/ogg");

    // Size in bytes (must match spring.servlet.multipart.max-file-size)
    public static final long SIZE_BYTES_MIN = 1L;
    public static final long MAX_UPLOAD_SIZE_BYTES = 100L * 1024 * 1024; // 100MB

    // Dimensions
    public static final int DIMENSION_MIN = 1;
    public static final int DIMENSION_MAX = 50000;

    // Duration in milliseconds
    public static final long DURATION_MS_MIN = 0L;
    public static final long DURATION_MS_MAX = 24L * 60 * 60 * 1000; // 24 hours
}
