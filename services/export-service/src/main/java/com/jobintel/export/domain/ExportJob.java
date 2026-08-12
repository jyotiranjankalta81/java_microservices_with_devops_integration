package com.jobintel.export.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "export_jobs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ExportJob {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private UUID userId;
    private String status;
    @Column(name = "filter_json", columnDefinition = "TEXT")
    private String filterJson;
    private String filePath;
    private String errorMessage;
    private Instant createdAt;
    private Instant completedAt;
}
