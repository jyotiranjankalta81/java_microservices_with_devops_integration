package com.jobintel.audit.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobintel.common.events.DomainEvent;
import com.jobintel.audit.domain.AuditRecord;
import com.jobintel.audit.repository.AuditRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventConsumer {

    private final AuditRecordRepository repository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = {"job.created", "job.updated", "job.deleted"}, groupId = "audit-service")
    @Transactional
    public void consume(String message) throws Exception {
        DomainEvent event = objectMapper.readValue(message, DomainEvent.class);
        if (repository.existsByEventId(event.getEventId())) return;
        repository.save(AuditRecord.builder()
                .eventId(event.getEventId())
                .eventType(event.getEventType())
                .aggregateId(event.getAggregateId())
                .payload(objectMapper.writeValueAsString(event.getPayload()))
                .correlationId(event.getCorrelationId())
                .build());
    }
}
