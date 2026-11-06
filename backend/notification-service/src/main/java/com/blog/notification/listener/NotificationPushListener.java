package com.blog.notification.listener;

import com.blog.notification.application.dto.response.NotificationResponse;
import com.blog.notification.event.NotificationsPersistedEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class NotificationPushListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationPushListener.class);

    private static final String USER_QUEUE = "/queue/notifications";

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationPushListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onNotificationsPersisted(NotificationsPersistedEvent event) {
        for (NotificationResponse notification : event.notifications()) {
            try {
                messagingTemplate.convertAndSendToUser(notification.recipientId().toString(), USER_QUEUE, notification);
            } catch (RuntimeException exception) {
                // Persistence already succeeded; a delivery failure only costs the live update.
                log.warn("Could not push notification {} to {}: {}",
                        notification.id(), notification.recipientId(), exception.getMessage());
            }
        }
    }
}
