package com.jobintel.dashboard.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobintel.common.events.DomainEvent;
import com.jobintel.dashboard.service.DashboardProjectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobEventConsumer {

    private final DashboardProjectionService projectionService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = {"job.created", "job.updated", "job.deleted"}, groupId = "dashboard-service")
    public void consume(String message) throws Exception {
        try {
            DomainEvent event = objectMapper.readValue(message, DomainEvent.class);
            if (projectionService.isProcessed(event.getEventId())) {
                log.debug("Skipping duplicate event {}", event.getEventId());
                return;
            }
            JsonNode payload = event.getPayload();
            projectionService.processEvent(event.getEventType(), payload, event.getEventId());
        } catch (Exception ex) {
            log.error("Failed to process dashboard event", ex);
            throw ex;
        }
    }
}
