package com.blog.notification.service;

import com.blog.notification.application.dto.request.CreateNotificationRequest;
import com.blog.notification.application.dto.response.NotificationResponse;
import com.blog.notification.application.dto.response.PageResponse;
import com.blog.notification.domain.entity.Notification;
import com.blog.notification.domain.model.NotificationType;
import com.blog.notification.exception.InvalidValueException;
import com.blog.notification.exception.NotificationNotFoundException;
import com.blog.notification.repository.NotificationRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    private static final UUID RECIPIENT_ID = UUID.randomUUID();
    private static final UUID ACTOR_ID = UUID.randomUUID();
    private static final UUID POST_ID = UUID.randomUUID();
    private static final UUID NOTIFICATION_ID = UUID.randomUUID();

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void fetchFirstPageAsksForOneExtraRowToDetectMoreContent() {
        when(notificationRepository.findFirstPage(eq(RECIPIENT_ID), any(Pageable.class)))
                .thenReturn(List.of(notification(NOTIFICATION_ID)));

        PageResponse<NotificationResponse> page = notificationService.fetchNotifications(
                RECIPIENT_ID, null, 20, false);

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(notificationRepository).findFirstPage(eq(RECIPIENT_ID), pageable.capture());

        assertThat(pageable.getValue().getPageSize()).isEqualTo(21);
        assertThat(page.content()).hasSize(1);
        assertThat(page.hasNext()).isFalse();
        assertThat(page.nextCursor()).isNull();
    }

    @Test
    void fetchMoreRowsThanRequestedTrimsAndReturnsLastIdAsCursor() {
        List<Notification> rows = List.of(notification(UUID.randomUUID()), notification(NOTIFICATION_ID),
                notification(UUID.randomUUID()));
        when(notificationRepository.findFirstPage(eq(RECIPIENT_ID), any(Pageable.class))).thenReturn(rows);

        PageResponse<NotificationResponse> page = notificationService.fetchNotifications(
                RECIPIENT_ID, null, 2, false);

        assertThat(page.content()).hasSize(2);
        assertThat(page.hasNext()).isTrue();
        assertThat(page.nextCursor()).isEqualTo(NOTIFICATION_ID.toString());
    }

    @Test
    void fetchWithCursorResumesFromTheCursorPositionOfTheRecipientsOwnNotification() {
        Instant cursorCreatedAt = Instant.parse("2026-10-01T10:00:00Z");
        when(notificationRepository.findByIdAndRecipientId(NOTIFICATION_ID, RECIPIENT_ID))
                .thenReturn(Optional.of(notification(NOTIFICATION_ID, cursorCreatedAt)));
        when(notificationRepository.findWithCursor(eq(RECIPIENT_ID), eq(NOTIFICATION_ID), eq(cursorCreatedAt),
                any(Pageable.class)))
                .thenReturn(List.of(notification(UUID.randomUUID())));

        PageResponse<NotificationResponse> page = notificationService.fetchNotifications(
                RECIPIENT_ID, NOTIFICATION_ID, 20, false);

        assertThat(page.content()).hasSize(1);
        verify(notificationRepository).findWithCursor(eq(RECIPIENT_ID), eq(NOTIFICATION_ID), eq(cursorCreatedAt),
                any(Pageable.class));
    }

    @Test
    void unreadOnlyFlagSelectsTheUnreadQueries() {
        when(notificationRepository.findFirstPageUnread(eq(RECIPIENT_ID), any(Pageable.class)))
                .thenReturn(List.of());

        notificationService.fetchNotifications(RECIPIENT_ID, null, 20, true);

        verify(notificationRepository).findFirstPageUnread(eq(RECIPIENT_ID), any(Pageable.class));
        verify(notificationRepository, never()).findFirstPage(any(), any());
    }

    @Test
    void cursorOwnedByAnotherRecipientIsNotResolved() {
        when(notificationRepository.findByIdAndRecipientId(NOTIFICATION_ID, RECIPIENT_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.fetchNotifications(RECIPIENT_ID, NOTIFICATION_ID, 20, false))
                .isInstanceOf(NotificationNotFoundException.class);

        verifyNoInteractionsOfCursorQueries();
    }

    @Test
    void createStoresOneNotificationPerRequestKeepingEveryColumn() {
        givenSavedNotifications();

        List<NotificationResponse> created = notificationService.create(List.of(
                new CreateNotificationRequest(RECIPIENT_ID, ACTOR_ID, NotificationType.NEW_POST, POST_ID, "new post"),
                new CreateNotificationRequest(RECIPIENT_ID, ACTOR_ID, NotificationType.LIKE, POST_ID, null)));

        ArgumentCaptor<List<Notification>> saved = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository).saveAll(saved.capture());

        assertThat(saved.getValue()).hasSize(2);
        assertThat(saved.getValue().get(0).getRecipientId()).isEqualTo(RECIPIENT_ID);
        assertThat(saved.getValue().get(0).getActorId()).isEqualTo(ACTOR_ID);
        assertThat(saved.getValue().get(0).getType()).isEqualTo(NotificationType.NEW_POST);
        assertThat(saved.getValue().get(0).getResourceId()).isEqualTo(POST_ID);
        assertThat(saved.getValue().get(0).getMessage()).isEqualTo("new post");
        assertThat(saved.getValue().get(0).isRead()).isFalse();
        assertThat(created).hasSize(2);
    }

    @Test
    void creatingANotificationAboutTheRecipientOwnActionIsRejected() {
        assertThatThrownBy(() -> notificationService.create(List.of(
                new CreateNotificationRequest(RECIPIENT_ID, RECIPIENT_ID, NotificationType.NEW_POST, POST_ID, null))))
                .isInstanceOf(InvalidValueException.class);

        verify(notificationRepository, never()).saveAll(any());
    }

    @Test
    void markingReadStampsTheReadingTime() {
        givenFoundUnreadNotification();

        NotificationResponse response = notificationService.updateReadState(
                NOTIFICATION_ID, RECIPIENT_ID, true);

        assertThat(response.read()).isTrue();
        assertThat(response.readAt()).isNotNull();
    }

    @Test
    void markingUnreadClearsTheReadingTime() {
        Notification notification = notification(NOTIFICATION_ID);
        notification.markRead();
        when(notificationRepository.findByIdAndRecipientId(NOTIFICATION_ID, RECIPIENT_ID))
                .thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(call -> call.getArgument(0));

        NotificationResponse response = notificationService.updateReadState(
                NOTIFICATION_ID, RECIPIENT_ID, false);

        assertThat(response.read()).isFalse();
        assertThat(response.readAt()).isNull();
    }

    @Test
    void notificationOfAnotherRecipientCannotBeUpdated() {
        when(notificationRepository.findByIdAndRecipientId(NOTIFICATION_ID, RECIPIENT_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.updateReadState(NOTIFICATION_ID, RECIPIENT_ID, true))
                .isInstanceOf(NotificationNotFoundException.class);

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void markAllReadAndUnreadCountAreBothScopedToTheRecipient() {
        when(notificationRepository.markAllRead(eq(RECIPIENT_ID), any(Instant.class))).thenReturn(3);
        when(notificationRepository.countUnread(RECIPIENT_ID)).thenReturn(3L);

        assertThat(notificationService.markAllRead(RECIPIENT_ID)).isEqualTo(3);
        assertThat(notificationService.countUnread(RECIPIENT_ID)).isEqualTo(3L);
    }

    @Test
    void deleteRemovesOnlyTheRecipientsOwnNotification() {
        when(notificationRepository.findByIdAndRecipientId(NOTIFICATION_ID, RECIPIENT_ID))
                .thenReturn(Optional.of(notification(NOTIFICATION_ID)));

        notificationService.delete(NOTIFICATION_ID, RECIPIENT_ID);

        verify(notificationRepository).delete(any(Notification.class));
    }

    @Test
    void deleteOfUnknownNotificationDoesNotReachTheRepositoryDelete() {
        when(notificationRepository.findByIdAndRecipientId(NOTIFICATION_ID, RECIPIENT_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.delete(NOTIFICATION_ID, RECIPIENT_ID))
                .isInstanceOf(NotificationNotFoundException.class);

        verify(notificationRepository, never()).delete(any(Notification.class));
    }

    private void verifyNoInteractionsOfCursorQueries() {
        verify(notificationRepository, never()).findWithCursor(any(), any(), any(), any());
        verify(notificationRepository, never()).findFirstPage(any(), any());
    }

    private void givenSavedNotifications() {
        when(notificationRepository.saveAll(any())).thenAnswer(call -> {
            List<Notification> notifications = call.getArgument(0);
            Notification first = notifications.get(0);
            ReflectionTestUtils.setField(first, "id", NOTIFICATION_ID);
            return notifications;
        });
    }

    private void givenFoundUnreadNotification() {
        when(notificationRepository.findByIdAndRecipientId(NOTIFICATION_ID, RECIPIENT_ID))
                .thenReturn(Optional.of(notification(NOTIFICATION_ID)));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(call -> call.getArgument(0));
    }

    private Notification notification(UUID id) {
        return notification(id, Instant.parse("2026-10-01T10:00:00Z"));
    }

    private Notification notification(UUID id, Instant createdAt) {
        Notification notification = new Notification(
                RECIPIENT_ID, ACTOR_ID, NotificationType.NEW_POST, POST_ID, "new post");
        ReflectionTestUtils.setField(notification, "id", id);
        ReflectionTestUtils.setField(notification, "createdAt", createdAt);
        ReflectionTestUtils.setField(notification, "updatedAt", createdAt);
        return notification;
    }
}
