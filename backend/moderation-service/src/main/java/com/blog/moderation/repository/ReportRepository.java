package com.blog.moderation.repository;

import com.blog.moderation.domain.entity.Report;
import com.blog.moderation.domain.model.ReportStatus;
import com.blog.moderation.domain.model.TargetType;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, UUID> {

    List<Report> findByStatusOrderByCreatedAtDesc(ReportStatus status);

    List<Report> findByTargetTypeOrderByCreatedAtDesc(TargetType targetType);

    List<Report> findByTargetIdOrderByCreatedAtDesc(UUID targetId);

    List<Report> findByStatusInOrderByCreatedAtDesc(List<ReportStatus> statuses);

}
