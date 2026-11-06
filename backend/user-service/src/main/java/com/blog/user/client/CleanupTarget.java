package com.blog.user.client;

import org.springframework.web.client.RestClient;

/**
 * A service User Service asks to release the data it owns for a deleted account.
 */
public record CleanupTarget(String serviceName, RestClient restClient) {
}
