package com.jobintel.job.service;

import com.jobintel.job.domain.entity.OutboxEvent;
import com.jobintel.job.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;

    @Transactional
    public OutboxEvent saveEvent(String aggregateType, String aggregateId, String eventType,
                                 String payloadJson, String correlationId) {
        OutboxEvent event = OutboxEvent.builder()
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payload(payloadJson)
                .status("PENDING")
                .build();
        return outboxEventRepository.save(event);
    }
}
