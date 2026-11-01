package com.blog.notification.controller;

import com.blog.notification.application.dto.request.CreateNotificationRequest;
import com.blog.notification.application.dto.request.CreateNotificationsRequest;
import com.blog.notification.application.dto.response.NotificationResponse;
import com.blog.notification.domain.model.NotificationType;
import com.blog.notification.service.NotificationService;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InternalNotificationControllerTest {

    private static final UUID RECIPIENT_ID = UUID.randomUUID();
    private static final UUID ACTOR_ID = UUID.randomUUID();
    private static final UUID POST_ID = UUID.randomUUID();
    private static final UUID NOTIFICATION_ID = UUID.randomUUID();

    private static final Instant CREATED_AT = Instant.parse("2026-11-01T10:00:00Z");

    private final NotificationService notificationService = mock(NotificationService.class);
    private final InternalNotificationController controller = new InternalNotificationController(notificationService);

    @Test
    void createdNotificationsAreReturnedSoTheCallerCanPublishThem() {
        when(notificationService.create(any())).thenReturn(List.of(notificationResponse()));

        ResponseEntity<List<NotificationResponse>> response = controller.create(
                new CreateNotificationsRequest(List.of(
                        new CreateNotificationRequest(RECIPIENT_ID, ACTOR_ID, NotificationType.NEW_POST, POST_ID,
                                "new post"))));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).containsExactly(notificationResponse());

        ArgumentCaptor<List<CreateNotificationRequest>> forwarded = ArgumentCaptor.forClass(List.class);
        verify(notificationService).create(forwarded.capture());
        assertThat(forwarded.getValue().get(0).recipientId()).isEqualTo(RECIPIENT_ID);
    }

    private NotificationResponse notificationResponse() {
        return new NotificationResponse(NOTIFICATION_ID, RECIPIENT_ID, ACTOR_ID, NotificationType.NEW_POST,
                POST_ID, "new post", false, null, CREATED_AT, CREATED_AT);
    }
}
