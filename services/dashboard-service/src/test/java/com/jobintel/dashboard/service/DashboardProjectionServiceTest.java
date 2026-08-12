package com.jobintel.dashboard.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobintel.dashboard.domain.DashboardSummary;
import com.jobintel.dashboard.dto.DashboardResponse;
import com.jobintel.dashboard.repository.DashboardSummaryRepository;
import com.jobintel.dashboard.repository.DimensionCountRepository;
import com.jobintel.dashboard.repository.ProcessedEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardProjectionServiceTest {

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @Mock
    private DashboardSummaryRepository summaryRepository;

    @Mock
    private DimensionCountRepository dimensionCountRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private DashboardProjectionService projectionService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    void getDashboard_returnsEmptySummaryWhenNoData() {
        when(summaryRepository.findById(userId)).thenReturn(Optional.empty());
        when(dimensionCountRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        DashboardResponse response = projectionService.getDashboard(userId);

        assertThat(response).isNotNull();
        assertThat(response.totalJobs()).isZero();
        assertThat(response.byCompany()).isEmpty();
    }

    @Test
    void getDashboard_returnsPopulatedSummary() {
        DashboardSummary summary = DashboardSummary.builder()
                .userId(userId)
                .totalJobs(5)
                .jobsToday(2)
                .jobsThisWeek(4)
                .jobsThisMonth(5)
                .build();

        when(summaryRepository.findById(userId)).thenReturn(Optional.of(summary));
        when(dimensionCountRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        DashboardResponse response = projectionService.getDashboard(userId);

        assertThat(response).isNotNull();
        assertThat(response.totalJobs()).isEqualTo(5);
        assertThat(response.jobsToday()).isEqualTo(2);
    }
}
