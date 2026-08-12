package com.jobintel.dashboard.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity @Table(name = "dimension_counts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DimensionCount {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private UUID userId;
    private String dimension;
    private String dimensionValue;
    private long count;
}
