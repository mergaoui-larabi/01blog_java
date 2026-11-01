package com.blog.notification.controller;

import com.blog.notification.application.dto.response.NotificationResponse;
import com.blog.notification.application.dto.response.PageResponse;
import com.blog.notification.domain.model.NotificationType;
import com.blog.notification.exception.NotificationNotFoundException;
import com.blog.notification.service.NotificationService;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    private static final UUID RECIPIENT_ID = UUID.randomUUID();
    private static final UUID ACTOR_ID = UUID.randomUUID();
    private static final UUID POST_ID = UUID.randomUUID();
    private static final UUID NOTIFICATION_ID = UUID.randomUUID();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void anonymousUserCannotListNotifications() throws Exception {
        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(notificationService);
    }

    @Test
    void listingIsAlwaysScopedToTheAuthenticatedRecipient() throws Exception {
        when(notificationService.fetchNotifications(eq(RECIPIENT_ID), any(), anyInt(), anyBoolean()))
                .thenReturn(new PageResponse<>(List.of(notificationResponse()), null, false));

        mockMvc.perform(get("/api/v1/notifications").with(jwt().jwt(token -> token.subject(RECIPIENT_ID.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].recipientId").value(RECIPIENT_ID.toString()))
                .andExpect(jsonPath("$.content[0].type").value(NotificationType.NEW_POST.name()));

        ArgumentCaptor<UUID> recipient = ArgumentCaptor.forClass(UUID.class);
        verify(notificationService).fetchNotifications(recipient.capture(), any(), anyInt(), anyBoolean());
        assertThat(recipient.getValue()).isEqualTo(RECIPIENT_ID);
    }

    @Test
    void cursorAndUnreadOnlyFlagsAreForwardedToTheService() throws Exception {
        when(notificationService.fetchNotifications(eq(RECIPIENT_ID), eq(NOTIFICATION_ID), eq(5), eq(true)))
                .thenReturn(new PageResponse<>(List.of(), "cursor", true));

        mockMvc.perform(get("/api/v1/notifications")
                        .param("cursor", NOTIFICATION_ID.toString())
                        .param("unreadOnly", "true")
                        .param("size", "5")
                        .with(jwt().jwt(token -> token.subject(RECIPIENT_ID.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.nextCursor").value("cursor"));
    }

    @Test
    void malformedCursorIsRejected() throws Exception {
        mockMvc.perform(get("/api/v1/notifications").param("cursor", "not-a-uuid").with(jwt()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(notificationService);
    }

    @Test
    void unreadCountIsReadFromTheAuthenticatedRecipientsBox() throws Exception {
        when(notificationService.countUnread(RECIPIENT_ID)).thenReturn(4L);

        mockMvc.perform(get("/api/v1/notifications/unread-count")
                        .with(jwt().jwt(token -> token.subject(RECIPIENT_ID.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(4));
    }

    @Test
    void readStateIsUpdatedWithoutTouchingAnotherRecipientsNotification() throws Exception {
        when(notificationService.updateReadState(eq(NOTIFICATION_ID), eq(RECIPIENT_ID), eq(false)))
                .thenReturn(notificationResponse());

        mockMvc.perform(patch("/api/v1/notifications/" + NOTIFICATION_ID)
                        .with(jwt().jwt(token -> token.subject(RECIPIENT_ID.toString())))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"read":false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(NOTIFICATION_ID.toString()))
                .andExpect(jsonPath("$.read").value(false));
    }

    @Test
    void readStateWithoutABodyIsRejected() throws Exception {
        mockMvc.perform(patch("/api/v1/notifications/" + NOTIFICATION_ID)
                        .with(jwt().jwt(token -> token.subject(RECIPIENT_ID.toString())))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(notificationService, never()).updateReadState(any(), any(), anyBoolean());
    }

    @Test
    void unknownNotificationCannotBeMarkedRead() throws Exception {
        when(notificationService.updateReadState(eq(NOTIFICATION_ID), eq(RECIPIENT_ID), eq(true)))
                .thenThrow(new NotificationNotFoundException());

        mockMvc.perform(patch("/api/v1/notifications/" + NOTIFICATION_ID)
                        .with(jwt().jwt(token -> token.subject(RECIPIENT_ID.toString())))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"read":true}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void wholeInboxCanBeMarkedReadAtOnce() throws Exception {
        mockMvc.perform(patch("/api/v1/notifications/read-all")
                        .with(jwt().jwt(token -> token.subject(RECIPIENT_ID.toString())))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(notificationService).markAllRead(RECIPIENT_ID);
    }

    @Test
    void recipientDeletesOneNotification() throws Exception {
        mockMvc.perform(delete("/api/v1/notifications/" + NOTIFICATION_ID)
                        .with(jwt().jwt(token -> token.subject(RECIPIENT_ID.toString())))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(notificationService).delete(NOTIFICATION_ID, RECIPIENT_ID);
    }

    @Test
    void deleteOfAnUnknownNotificationIsReportedAsNotFound() throws Exception {
        doThrow(new NotificationNotFoundException()).when(notificationService).delete(NOTIFICATION_ID, RECIPIENT_ID);

        mockMvc.perform(delete("/api/v1/notifications/" + NOTIFICATION_ID)
                        .with(jwt().jwt(token -> token.subject(RECIPIENT_ID.toString())))
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    private NotificationResponse notificationResponse() {
        return new NotificationResponse(NOTIFICATION_ID, RECIPIENT_ID, ACTOR_ID, NotificationType.NEW_POST,
                POST_ID, "new post", false, null, Instant.now(), Instant.now());
    }
}
