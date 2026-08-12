package com.jobintel.audit.repository;

import com.jobintel.audit.domain.AuditRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface AuditRecordRepository extends JpaRepository<AuditRecord, Long> {
    boolean existsByEventId(UUID eventId);
    Page<AuditRecord> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
