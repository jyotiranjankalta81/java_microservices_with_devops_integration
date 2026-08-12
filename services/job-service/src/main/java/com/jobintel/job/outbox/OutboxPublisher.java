package com.jobintel.job.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobintel.common.events.DomainEvent;
import com.jobintel.common.events.KafkaTopics;
import com.jobintel.common.web.CorrelationIdFilter;
import com.jobintel.job.domain.entity.OutboxEvent;
import com.jobintel.job.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "${job.outbox.poll-interval-ms:2000}")
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pending = outboxEventRepository.findPendingEvents();
        for (OutboxEvent event : pending) {
            try {
                String topic = mapTopic(event.getEventType());
                DomainEvent domainEvent = DomainEvent.builder()
                        .eventId(UUID.randomUUID())
                        .eventType(event.getEventType())
                        .timestamp(Instant.now())
                        .source("job-service")
                        .correlationId(CorrelationIdFilter.getCorrelationId())
                        .traceId(MDC.get("traceId"))
                        .aggregateId(event.getAggregateId())
                        .payload(objectMapper.readTree(event.getPayload()))
                        .build();
                String message = objectMapper.writeValueAsString(domainEvent);
                kafkaTemplate.send(topic, event.getAggregateId(), message);
                event.setStatus("PUBLISHED");
                event.setPublishedAt(Instant.now());
                outboxEventRepository.save(event);
                log.debug("Published outbox event id={} type={}", event.getId(), event.getEventType());
            } catch (Exception ex) {
                log.error("Failed to publish outbox event id={}", event.getId(), ex);
            }
        }
    }

    private String mapTopic(String eventType) {
        return switch (eventType) {
            case "job.created" -> KafkaTopics.JOB_CREATED;
            case "job.updated" -> KafkaTopics.JOB_UPDATED;
            case "job.deleted" -> KafkaTopics.JOB_DELETED;
            default -> eventType;
        };
    }
}
