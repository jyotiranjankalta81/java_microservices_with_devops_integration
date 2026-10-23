package com.jobintel.evidencing.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "publication_dlq_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicationDlqEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "queue_name", nullable = false, length = 100)
    private String queueName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "error_reason", nullable = false, columnDefinition = "TEXT")
    private String errorReason;

    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private int retryCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
