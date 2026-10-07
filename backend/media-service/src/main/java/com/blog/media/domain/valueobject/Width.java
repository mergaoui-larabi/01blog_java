package com.blog.media.domain.valueobject;

import com.blog.media.constant.ValidationConstants;
import com.blog.media.exception.InvalidValueException;

import jakarta.persistence.Embeddable;

/**
 * Width in pixels. A null value means the dimension is unknown
 * (e.g. video content, or an image format we cannot inspect).
 */
@Embeddable
public final class Width {

    private Integer value;

    // Required by JPA
    protected Width() {
    }

    public Width(Integer value) {
        if (value != null) {
            if (value < ValidationConstants.DIMENSION_MIN) {
                throw new InvalidValueException("Width must be at least "
                        + ValidationConstants.DIMENSION_MIN + " pixel");
            }
            if (value > ValidationConstants.DIMENSION_MAX) {
                throw new InvalidValueException("Width must not exceed "
                        + ValidationConstants.DIMENSION_MAX + " pixels");
            }
        }

        this.value = value;
    }

    public Integer value() {
        return value;
    }

    @Override
    public String toString() {
        return value == null ? null : value.toString();
    }
}
