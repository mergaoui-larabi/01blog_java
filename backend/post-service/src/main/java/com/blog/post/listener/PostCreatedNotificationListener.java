package com.blog.post.listener;

import com.blog.post.client.NotificationServiceClient;
import com.blog.post.event.PostCreatedEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PostCreatedNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(PostCreatedNotificationListener.class);

    private final NotificationServiceClient notificationServiceClient;

    public PostCreatedNotificationListener(NotificationServiceClient notificationServiceClient) {
        this.notificationServiceClient = notificationServiceClient;
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPostCreated(PostCreatedEvent event) {
        try {
            notificationServiceClient.notifyPostCreated(event.postId(), event.authorId());
        } catch (RuntimeException exception) {
            // The post is already committed: swallowing this keeps the failure away from the
            // transaction and from the creation response.
            log.error("Post {} is committed but its followers were not notified: {}",
                    event.postId(), exception.getMessage());
        }
    }
}
