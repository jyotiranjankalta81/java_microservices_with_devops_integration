package com.jobintel.evidencing.repository;

import com.jobintel.evidencing.domain.entity.PostRecipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostRecipientRepository extends JpaRepository<PostRecipient, UUID> {

    List<PostRecipient> findByPostId(UUID postId);

    boolean existsByPostIdAndUserId(UUID postId, UUID userId);

    @Query("SELECT pr.post.id FROM PostRecipient pr WHERE pr.userId = :userId")
    List<UUID> findPostIdsByUserId(@Param("userId") UUID userId);

    void deleteByPostId(UUID postId);
}
