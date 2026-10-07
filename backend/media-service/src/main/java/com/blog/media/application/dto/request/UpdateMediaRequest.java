package com.blog.media.application.dto.request;

import org.springframework.web.multipart.MultipartFile;

/**
 * Update payload: either a replacement file, a new purpose, or both.
 * Sent as multipart/form-data; both parts are optional.
 */
public record UpdateMediaRequest(
                MultipartFile file,
                String purpose) {
}
