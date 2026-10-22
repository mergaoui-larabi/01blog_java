package com.blog.post.client;

import java.util.List;
import java.util.UUID;

public interface MediaServiceClient {
    void validateMediaOwnership(UUID mediaId, String authorizationHeader);
    void validateAllMediaOwnership(List<UUID> mediaIds, String authorizationHeader);
}
