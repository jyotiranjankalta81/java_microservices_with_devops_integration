package com.jobintel.dashboard.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "processed_events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProcessedEvent {
    @Id
    private UUID eventId;

    @org.hibernate.annotations.CreationTimestamp
    @Column(name = "processed_at", updatable = false)
    private Instant processedAt;
}
