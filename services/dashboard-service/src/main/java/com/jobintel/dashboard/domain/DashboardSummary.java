package com.jobintel.dashboard.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "dashboard_summary")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DashboardSummary {
    @Id
    private UUID userId;
    private long totalJobs;
    private long jobsToday;
    private long jobsThisWeek;
    private long jobsThisMonth;
    private Instant updatedAt;

    public static DashboardSummary empty(UUID userId) {
        return DashboardSummary.builder()
                .userId(userId).totalJobs(0).jobsToday(0).jobsThisWeek(0).jobsThisMonth(0)
                .updatedAt(Instant.now()).build();
    }
}
