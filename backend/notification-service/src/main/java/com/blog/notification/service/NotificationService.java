package com.blog.notification.service;

import com.blog.notification.application.dto.request.CreateNotificationRequest;
import com.blog.notification.application.dto.response.NotificationResponse;
import com.blog.notification.application.dto.response.PageResponse;
import com.blog.notification.domain.entity.Notification;
import com.blog.notification.exception.NotificationNotFoundException;
import com.blog.notification.repository.NotificationRepository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public List<NotificationResponse> create(List<CreateNotificationRequest> requests) {
        List<Notification> notifications = requests.stream()
                .map(request -> new Notification(
                        request.recipientId(),
                        request.actorId(),
                        request.type(),
                        request.resourceId(),
                        request.message()))
                .toList();

        return notificationRepository.saveAll(notifications).stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> fetchNotifications(
            UUID recipientId, UUID cursor, int size, boolean unreadOnly) {

        Pageable pageable = PageRequest.ofSize(size + 1);
        List<Notification> notifications;

        if (cursor == null) {
            notifications = unreadOnly
                    ? notificationRepository.findFirstPageUnread(recipientId, pageable)
                    : notificationRepository.findFirstPage(recipientId, pageable);
        } else {
            Instant cursorCreatedAt = requireNotification(cursor, recipientId).getCreatedAt();
            notifications = unreadOnly
                    ? notificationRepository.findWithCursorUnread(recipientId, cursor, cursorCreatedAt, pageable)
                    : notificationRepository.findWithCursor(recipientId, cursor, cursorCreatedAt, pageable);
        }

        return buildPageResponse(notifications, size);
    }

    @Transactional(readOnly = true)
    public long countUnread(UUID recipientId) {
        return notificationRepository.countUnread(recipientId);
    }

    @Transactional
    public NotificationResponse updateReadState(UUID notificationId, UUID recipientId, boolean read) {
        Notification notification = requireNotification(notificationId, recipientId);

        if (read) {
            notification.markRead();
        } else {
            notification.markUnread();
        }

        return NotificationResponse.from(notificationRepository.save(notification));
    }

    @Transactional
    public int markAllRead(UUID recipientId) {
        return notificationRepository.markAllRead(recipientId, Instant.now());
    }

    @Transactional
    public void delete(UUID notificationId, UUID recipientId) {
        notificationRepository.delete(requireNotification(notificationId, recipientId));
    }

    private Notification requireNotification(UUID notificationId, UUID recipientId) {
        return notificationRepository.findByIdAndRecipientId(notificationId, recipientId)
                .orElseThrow(NotificationNotFoundException::new);
    }

    private PageResponse<NotificationResponse> buildPageResponse(List<Notification> notifications, int size) {
        boolean hasNext = notifications.size() > size;
        List<Notification> contentNotifications = hasNext ? notifications.subList(0, size) : notifications;
        String nextCursor = hasNext && !contentNotifications.isEmpty()
                ? contentNotifications.get(contentNotifications.size() - 1).getId().toString()
                : null;

        List<NotificationResponse> responses = contentNotifications.stream()
                .map(NotificationResponse::from)
                .toList();

        return new PageResponse<>(responses, nextCursor, hasNext);
    }
}
