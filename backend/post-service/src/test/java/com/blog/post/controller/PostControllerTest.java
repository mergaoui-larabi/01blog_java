package com.blog.post.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.blog.post.application.dto.post.PageResponse;
import com.blog.post.application.dto.post.PostResponse;
import com.blog.post.exception.GlobalExceptionHandler;
import com.blog.post.exception.InvalidMediaException;
import com.blog.post.exception.PostNotFoundException;
import com.blog.post.exception.PostNotOwnedException;
import com.blog.post.exception.UserServiceUnavailableException;
import com.blog.post.service.PostService;

@ExtendWith(MockitoExtension.class)
class PostControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PostService postService;

    @InjectMocks
    private PostController postController;

    private UUID userId;
    private Jwt mockJwt;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        mockJwt = new Jwt(
                "token-value",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "none"),
                Map.of("sub", userId.toString(), "role", "USER"));

        HandlerMethodArgumentResolver jwtResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter,
                                          ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest,
                                          WebDataBinderFactory binderFactory) {
                return mockJwt;
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(postController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(jwtResolver)
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/posts/feed returns PageResponse with posts")
    void fetchFeed_Success() throws Exception {
        UUID postId = UUID.randomUUID();
        PostResponse postResponse = new PostResponse(
                postId.toString(),
                userId.toString(),
                "Feed post content",
                List.of("media-1", "media-2"),
                5L,
                2L,
                false,
                false,
                Instant.now(),
                Instant.now());

        PageResponse<PostResponse> pageResponse = new PageResponse<>(
                List.of(postResponse),
                null,
                false);

        when(postService.fetchFeed(eq(userId), any(), eq(20))).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/posts/feed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(postId.toString()))
                .andExpect(jsonPath("$.content[0].content").value("Feed post content"))
                .andExpect(jsonPath("$.content[0].mediaIds[0]").value("media-1"))
                .andExpect(jsonPath("$.content[0].mediaIds[1]").value("media-2"))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    @DisplayName("GET /api/v1/posts/feed returns 503 when User Service is down")
    void fetchFeed_UserServiceDown_Returns503() throws Exception {
        when(postService.fetchFeed(eq(userId), any(), eq(20)))
                .thenThrow(new UserServiceUnavailableException("User Service unavailable"));

        mockMvc.perform(get("/api/v1/posts/feed"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.message").value("User Service unavailable"));
    }

    @Test
    @DisplayName("POST /api/v1/posts creates a post")
    void createPost_Success() throws Exception {
        UUID postId = UUID.randomUUID();
        PostResponse postResponse = new PostResponse(
                postId.toString(),
                userId.toString(),
                "Created content",
                List.of("media-uuid-1"),
                0L,
                0L,
                false,
                false,
                Instant.now(),
                Instant.now());

        when(postService.createPost(any(), eq(userId), any())).thenReturn(postResponse);

        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "content": "Created content",
                                    "mediaIds": ["media-uuid-1"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(postId.toString()))
                .andExpect(jsonPath("$.content").value("Created content"))
                .andExpect(jsonPath("$.mediaIds[0]").value("media-uuid-1"));
    }

    @Test
    @DisplayName("POST /api/v1/posts with invalid media returns 400 Bad Request")
    void createPost_InvalidMedia_Returns400() throws Exception {
        when(postService.createPost(any(), eq(userId), any()))
                .thenThrow(new InvalidMediaException("Invalid media attachment"));

        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "content": "Post with bad media",
                                    "mediaIds": ["bad-media-id"]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid media attachment"));
    }

    @Test
    @DisplayName("DELETE /api/v1/posts/{postId} not owned by caller returns 403 Forbidden")
    void deletePost_NotOwned_Returns403() throws Exception {
        UUID postId = UUID.randomUUID();

        org.mockito.Mockito.doThrow(new PostNotOwnedException("Post not owned by current user: " + postId))
                .when(postService).deletePost(postId, userId);

        mockMvc.perform(delete("/api/v1/posts/{postId}", postId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("GET /api/v1/posts/{postId} not found returns 404")
    void fetchPost_NotFound_Returns404() throws Exception {
        UUID postId = UUID.randomUUID();

        when(postService.fetchPost(eq(postId), any()))
                .thenThrow(new PostNotFoundException("Post not found: " + postId));

        mockMvc.perform(get("/api/v1/posts/{postId}", postId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
