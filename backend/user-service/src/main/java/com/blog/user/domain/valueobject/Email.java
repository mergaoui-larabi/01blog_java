package com.blog.user.domain.valueobject;

import com.blog.user.exception.InvalidValueException;
import jakarta.persistence.Embeddable;

import com.blog.user.constant.ValidationConstants;

@Embeddable
public final class Email {

    private String value;

    // Required by JPA
    protected Email() {
    }

    public Email(String value) {
        if (value == null) {
            throw new InvalidValueException("Email cannot be null");
        }
        value = value.trim();
        if (value.isBlank()) {
            throw new InvalidValueException("Email cannot be blank");
        }
        if (value.length() > ValidationConstants.EMAIL_MAX_LENGTH) {
            throw new InvalidValueException("Email must not exceed "
                    + ValidationConstants.EMAIL_MAX_LENGTH
                    + " characters");
        }
        if (!ValidationConstants.EMAIL_REGEX_PATTERN.matcher(value).matches()) {
            throw new InvalidValueException("Invalid email format");
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