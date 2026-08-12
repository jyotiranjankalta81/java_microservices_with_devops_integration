package com.jobintel.common.events;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
@Jacksonized
public class DomainEvent {

    @Builder.Default
    UUID eventId = UUID.randomUUID();
    String eventType;
    @Builder.Default
    Instant timestamp = Instant.now();
    String source;
    String correlationId;
    String traceId;
    String aggregateId;
    JsonNode payload;
}
