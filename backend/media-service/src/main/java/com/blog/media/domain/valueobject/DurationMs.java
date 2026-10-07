package com.blog.media.domain.valueobject;

import com.blog.media.constant.ValidationConstants;
import com.blog.media.exception.InvalidValueException;

import jakarta.persistence.Embeddable;

/**
 * Duration in milliseconds. A null value means the duration is unknown
 * (e.g. image content).
 */
@Embeddable
public final class DurationMs {

    private Long value;

    // Required by JPA
    protected DurationMs() {
    }

    public DurationMs(Long value) {
        if (value != null) {
            if (value < ValidationConstants.DURATION_MS_MIN) {
                throw new InvalidValueException("Duration must not be negative");
            }
            if (value > ValidationConstants.DURATION_MS_MAX) {
                throw new InvalidValueException("Duration must not exceed "
                        + ValidationConstants.DURATION_MS_MAX + " milliseconds");
            }
        }

        this.value = value;
    }

    public Long value() {
        return value;
    }

    @Override
    public String toString() {
        return value == null ? null : value.toString();
    }
}
