package com.blog.post.repository;

import com.blog.post.domain.model.PostLike;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, UUID> {

    List<PostLike> findByPostId(UUID postId);

    List<PostLike> findByUserId(UUID userId);

}
