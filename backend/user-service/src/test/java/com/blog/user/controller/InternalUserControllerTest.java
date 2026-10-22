package com.blog.user.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.blog.user.service.FollowerService;
import com.blog.user.service.UserService;

@WebMvcTest(controllers = InternalUserController.class)
@AutoConfigureMockMvc(addFilters = false)
class InternalUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private FollowerService followerService;

    @Test
    @DisplayName("GET /internal/users/{userId}/following returns followed user IDs")
    void getFollowedUsers_Success() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID followedId1 = UUID.randomUUID();
        UUID followedId2 = UUID.randomUUID();

        when(followerService.getFollowedUserIds(userId)).thenReturn(List.of(followedId1, followedId2));

        mockMvc.perform(get("/internal/users/{userId}/following", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.followedUserIds.length()").value(2))
                .andExpect(jsonPath("$.followedUserIds[0]").value(followedId1.toString()))
                .andExpect(jsonPath("$.followedUserIds[1]").value(followedId2.toString()));
    }

    @Test
    @DisplayName("GET /internal/users/{userId}/following with empty list returns empty array")
    void getFollowedUsers_Empty() throws Exception {
        UUID userId = UUID.randomUUID();

        when(followerService.getFollowedUserIds(userId)).thenReturn(List.of());

        mockMvc.perform(get("/internal/users/{userId}/following", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.followedUserIds.length()").value(0));
    }
}
