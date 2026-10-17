package com.blog.moderation.repository;

import com.blog.moderation.domain.entity.ModerationAction;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModerationActionRepository extends JpaRepository<ModerationAction, UUID> {

    List<ModerationAction> findByReportIdOrderByCreatedAtDesc(UUID reportId);

}
