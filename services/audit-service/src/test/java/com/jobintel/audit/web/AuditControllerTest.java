package com.jobintel.audit.web;

import com.jobintel.audit.domain.AuditRecord;
import com.jobintel.audit.repository.AuditRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditControllerTest {

    @Mock
    private AuditRecordRepository auditRecordRepository;

    @InjectMocks
    private AuditController auditController;

    @Test
    void getAuditRecords_returnsPaginatedAuditLogs() {
        AuditRecord record = AuditRecord.builder()
                .id(1L)
                .eventId(UUID.randomUUID())
                .eventType("job.created")
                .aggregateId("100")
                .createdAt(Instant.now())
                .build();

        Page<AuditRecord> page = new PageImpl<>(List.of(record));
        when(auditRecordRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(page);

        Page<AuditRecord> result = auditController.getAuditRecords(PageRequest.of(0, 20));

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getEventType()).isEqualTo("job.created");
    }
}
