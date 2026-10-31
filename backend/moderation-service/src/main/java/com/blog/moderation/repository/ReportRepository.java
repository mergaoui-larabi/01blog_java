package com.blog.moderation.repository;

import com.blog.moderation.domain.entity.Report;
import com.blog.moderation.domain.model.ReportStatus;
import com.blog.moderation.domain.model.TargetType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ReportRepository extends JpaRepository<Report, UUID> {

    @Query("""
                    SELECT r FROM Report r
                    ORDER BY r.createdAt DESC, r.id DESC
                    """)
    List<Report> findFirstPage(Pageable pageable);

    @Query("""
                    SELECT r FROM Report r
                    WHERE (r.createdAt < :cursorCreatedAt OR (r.createdAt = :cursorCreatedAt AND r.id < :cursorId))
                    ORDER BY r.createdAt DESC, r.id DESC
                    """)
    List<Report> findWithCursor(
                    @Param("cursorId") UUID cursorId,
                    @Param("cursorCreatedAt") Instant cursorCreatedAt,
                    Pageable pageable);

    @Query("""
                    SELECT r FROM Report r
                    WHERE r.status = :status
                    ORDER BY r.createdAt DESC, r.id DESC
                    """)
    List<Report> findFirstPageByStatus(@Param("status") ReportStatus status, Pageable pageable);

    @Query("""
                    SELECT r FROM Report r
                    WHERE r.status = :status
                      AND (r.createdAt < :cursorCreatedAt OR (r.createdAt = :cursorCreatedAt AND r.id < :cursorId))
                    ORDER BY r.createdAt DESC, r.id DESC
                    """)
    List<Report> findWithCursorByStatus(
                    @Param("status") ReportStatus status,
                    @Param("cursorId") UUID cursorId,
                    @Param("cursorCreatedAt") Instant cursorCreatedAt,
                    Pageable pageable);

    List<Report> findByReporterIdOrderByCreatedAtDesc(UUID reporterId);

    List<Report> findByTargetTypeAndTargetIdOrderByCreatedAtDesc(TargetType targetType, UUID targetId);
}
