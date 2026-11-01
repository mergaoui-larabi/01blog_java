package com.blog.notification.service;

import com.blog.notification.application.dto.request.CreateNotificationRequest;
import com.blog.notification.application.dto.response.NotificationResponse;
import com.blog.notification.application.dto.response.PageResponse;
import com.blog.notification.domain.model.NotificationType;
import com.blog.notification.repository.NotificationRepository;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Disabled("Requires live PostgreSQL database on port 5435")
@Transactional
class NotificationLiveTest {

    private static final UUID RECIPIENT_ID = UUID.randomUUID();
    private static final UUID ACTOR_ID = UUID.randomUUID();

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    void everyQueryRunsAgainstTheRealSchema() {
        List<NotificationResponse> created = notificationService.create(List.of(
                new CreateNotificationRequest(RECIPIENT_ID, ACTOR_ID, NotificationType.NEW_POST, UUID.randomUUID(),
                        "first"),
                new CreateNotificationRequest(RECIPIENT_ID, ACTOR_ID, NotificationType.LIKE, UUID.randomUUID(),
                        "second"),
                new CreateNotificationRequest(RECIPIENT_ID, ACTOR_ID, NotificationType.COMMENT, UUID.randomUUID(),
                        "third")));
        notificationRepository.flush();

        PageResponse<NotificationResponse> firstChunk = notificationService.fetchNotifications(
                RECIPIENT_ID, null, 2, false);
        assertThat(firstChunk.content()).hasSize(2);
        assertThat(firstChunk.hasNext()).isTrue();
        assertThat(firstChunk.nextCursor()).isNotBlank();

        PageResponse<NotificationResponse> secondChunk = notificationService.fetchNotifications(
                RECIPIENT_ID, UUID.fromString(firstChunk.nextCursor()), 2, false);
        assertThat(secondChunk.content()).hasSize(1);
        assertThat(secondChunk.hasNext()).isFalse();

        PageResponse<NotificationResponse> unread = notificationService.fetchNotifications(
                RECIPIENT_ID, null, 2, true);
        assertThat(unread.content()).hasSize(2);
        assertThat(notificationService.countUnread(RECIPIENT_ID)).isEqualTo(3);

        NotificationResponse updated = notificationService.updateReadState(
                created.get(0).id(), RECIPIENT_ID, true);
        assertThat(updated.read()).isTrue();
        assertThat(updated.readAt()).isNotNull();

        assertThat(notificationService.markAllRead(RECIPIENT_ID)).isEqualTo(2);
        assertThat(notificationService.countUnread(RECIPIENT_ID)).isZero();

        assertThat(notificationService.updateReadState(created.get(1).id(), RECIPIENT_ID, false).read()).isFalse();
        assertThat(notificationService.fetchNotifications(RECIPIENT_ID, null, 2, true).content()).hasSize(1);

        notificationService.delete(created.get(2).id(), RECIPIENT_ID);
        assertThat(notificationRepository.findByIdAndRecipientId(created.get(2).id(), RECIPIENT_ID)).isEmpty();
    }
}
