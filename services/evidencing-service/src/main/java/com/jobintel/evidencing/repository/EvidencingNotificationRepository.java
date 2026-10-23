package com.jobintel.evidencing.repository;

import com.jobintel.evidencing.domain.entity.EvidencingNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EvidencingNotificationRepository extends JpaRepository<EvidencingNotification, UUID> {

    List<EvidencingNotification> findByUserIdOrderByCreatedAtDesc(UUID userId);

    long countByUserIdAndReadFalse(UUID userId);

    Optional<EvidencingNotification> findByIdempotencyKey(String idempotencyKey);
}
