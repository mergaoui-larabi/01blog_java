package com.blog.post.constant;

import java.util.regex.Pattern;

public final class ValidationConstants {
    private ValidationConstants() {
    }

    // comment
    public static final int COMMENT_CONTENT_MAX_LENGTH = 1024 * 1024;
    // post
    public static final int POST_CONTENT_MAX_LENGTH = 10 * 1024 * 1024;
}
