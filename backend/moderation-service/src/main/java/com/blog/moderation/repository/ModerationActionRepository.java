package com.blog.moderation.repository;

import com.blog.moderation.domain.entity.ModerationAction;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ModerationActionRepository extends JpaRepository<ModerationAction, UUID> {

    boolean existsByReportId(UUID reportId);

    List<ModerationAction> findByReportIdOrderByCreatedAtAsc(UUID reportId);
}
