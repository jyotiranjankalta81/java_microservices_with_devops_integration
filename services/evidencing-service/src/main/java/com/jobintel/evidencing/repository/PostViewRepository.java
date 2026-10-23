package com.jobintel.evidencing.repository;

import com.jobintel.evidencing.domain.entity.PostView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PostViewRepository extends JpaRepository<PostView, UUID> {

    long countByPostId(UUID postId);

    boolean existsByPostIdAndUserId(UUID postId, UUID userId);
}
