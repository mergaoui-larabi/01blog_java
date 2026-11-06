package com.blog.notification.repository;

import com.blog.notification.domain.entity.Notification;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Optional<Notification> findByIdAndRecipientId(UUID id, UUID recipientId);

    @Query("""
                    SELECT n FROM Notification n
                    WHERE n.recipientId = :recipientId
                    ORDER BY n.createdAt DESC, n.id DESC
                    """)
    List<Notification> findFirstPage(@Param("recipientId") UUID recipientId, Pageable pageable);

    @Query("""
                    SELECT n FROM Notification n
                    WHERE n.recipientId = :recipientId
                      AND (n.createdAt < :cursorCreatedAt OR (n.createdAt = :cursorCreatedAt AND n.id < :cursorId))
                    ORDER BY n.createdAt DESC, n.id DESC
                    """)
    List<Notification> findWithCursor(
                    @Param("recipientId") UUID recipientId,
                    @Param("cursorId") UUID cursorId,
                    @Param("cursorCreatedAt") Instant cursorCreatedAt,
                    Pageable pageable);

    @Query("""
                    SELECT n FROM Notification n
                    WHERE n.recipientId = :recipientId AND n.read = false
                    ORDER BY n.createdAt DESC, n.id DESC
                    """)
    List<Notification> findFirstPageUnread(@Param("recipientId") UUID recipientId, Pageable pageable);

    @Query("""
                    SELECT n FROM Notification n
                    WHERE n.recipientId = :recipientId
                      AND n.read = false
                      AND (n.createdAt < :cursorCreatedAt OR (n.createdAt = :cursorCreatedAt AND n.id < :cursorId))
                    ORDER BY n.createdAt DESC, n.id DESC
                    """)
    List<Notification> findWithCursorUnread(
                    @Param("recipientId") UUID recipientId,
                    @Param("cursorId") UUID cursorId,
                    @Param("cursorCreatedAt") Instant cursorCreatedAt,
                    Pageable pageable);

    @Query("""
                    SELECT COUNT(n) FROM Notification n
                    WHERE n.recipientId = :recipientId AND n.read = false
                    """)
    long countUnread(@Param("recipientId") UUID recipientId);

    // Cleared because a bulk update bypasses the persistence context and would leave loaded
    // notifications unread in memory while the database already marks them read
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                    UPDATE Notification n
                    SET n.read = true, n.readAt = :now, n.updatedAt = :now
                    WHERE n.recipientId = :recipientId AND n.read = false
                    """)
    int markAllRead(@Param("recipientId") UUID recipientId, @Param("now") Instant now);

    // A deleted account keeps neither its own notifications nor notifications about its actions
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                    DELETE FROM Notification n
                    WHERE n.recipientId = :userId OR n.actorId = :userId
                    """)
    long deleteForUser(@Param("userId") UUID userId);
}
