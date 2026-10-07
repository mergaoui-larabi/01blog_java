package com.blog.media.domain.valueobject;

import com.blog.media.constant.ValidationConstants;
import com.blog.media.exception.InvalidValueException;

import jakarta.persistence.Embeddable;

@Embeddable
public final class DetectedContentType {

    private String value;

    // Required by JPA
    protected DetectedContentType() {
    }

    public DetectedContentType(String value) {
        if (value == null) {
            throw new InvalidValueException("Detected content type cannot be null");
        }
        value = value.trim();
        if (value.isBlank()) {
            throw new InvalidValueException("Detected content type cannot be blank");
        }
        if (value.length() > ValidationConstants.CONTENT_TYPE_MAX_LENGTH) {
            throw new InvalidValueException("Detected content type must not exceed "
                    + ValidationConstants.CONTENT_TYPE_MAX_LENGTH + " characters");
        }
        if (!ValidationConstants.CONTENT_TYPE_REGEX_PATTERN.matcher(value).matches()) {
            throw new InvalidValueException("Invalid content type format: " + value);
        }
        if (!ValidationConstants.ALLOWED_CONTENT_TYPES.contains(value)) {
            throw new InvalidValueException("Unsupported content type: " + value
                    + ". Allowed types: " + ValidationConstants.ALLOWED_CONTENT_TYPES);
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
}
