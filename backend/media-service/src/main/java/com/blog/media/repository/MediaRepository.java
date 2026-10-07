package com.blog.media.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blog.media.domain.entity.Media;

public interface MediaRepository extends JpaRepository<Media, UUID> {
}
