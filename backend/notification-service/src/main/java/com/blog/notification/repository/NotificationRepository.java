package com.blog.notification.repository;

import com.blog.notification.domain.entity.Notification;
import com.blog.notification.domain.model.NotificationType;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByRecipientIdOrderByCreatedAtAsc(UUID recipientId);

    List<Notification> findByRecipientIdAndTypeOrderByCreatedAtAsc(UUID recipientId, NotificationType type);

    long countByRecipientIdAndReadAtIsNull(UUID recipientId);

}
