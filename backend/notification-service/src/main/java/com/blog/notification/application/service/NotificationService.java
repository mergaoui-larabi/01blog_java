package com.blog.notification.application.service;

import com.blog.notification.domain.entity.Notification;
import com.blog.notification.domain.model.NotificationType;
import com.blog.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public Notification createNotification(UUID recipientId, NotificationType type) {
        Notification notification = new Notification(recipientId, type);
        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<Notification> getNotificationsByRecipientId(UUID recipientId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtAsc(recipientId);
    }

    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotificationsByRecipientId(UUID recipientId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtAsc(recipientId);
    }

    @Transactional
    public void markAsRead(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.markRead();
        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public long countUnreadByRecipientId(UUID recipientId) {
        return notificationRepository.countByRecipientIdAndReadAtIsNull(recipientId);
    }
}
