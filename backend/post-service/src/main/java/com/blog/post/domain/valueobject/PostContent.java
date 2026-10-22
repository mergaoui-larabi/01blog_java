package com.blog.post.domain.valueobject;

import com.blog.post.constant.ValidationConstants;
import com.blog.post.exception.InvalidValueException;

public final class PostContent {

    private  String value;

    // Required by JPA
    protected PostContent() {
    }

    public PostContent(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidValueException("Post content cannot be null or blank");
        }
        if (value.length() > ValidationConstants.POST_CONTENT_MAX_LENGTH) {
            throw new InvalidValueException(
                    "Post content must not exceed " + ValidationConstants.POST_CONTENT_MAX_LENGTH + " characters");
        }
        this.value = value;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PostContent that)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
