package com.jobintel.job.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "job_sources")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 500)
    private String url;

    @Column(name = "source_type", length = 50)
    private String sourceType;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean active = true;
}
