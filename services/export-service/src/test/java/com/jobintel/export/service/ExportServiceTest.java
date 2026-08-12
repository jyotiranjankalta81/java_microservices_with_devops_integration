package com.jobintel.export.service;

import com.jobintel.export.domain.ExportJob;
import com.jobintel.export.dto.ExportRequest;
import com.jobintel.export.dto.ExportStatusResponse;
import com.jobintel.export.repository.ExportJobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExportServiceTest {

    @Mock
    private ExportJobRepository exportJobRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @InjectMocks
    private ExportService exportService;

    private UUID userId;
    private UUID jobId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        jobId = UUID.randomUUID();
    }

    @Test
    void requestExport_createsAndSavesJob() throws Exception {
        ExportRequest request = new ExportRequest("Google", "USA", "San Francisco", "Backend");
        ExportJob savedJob = ExportJob.builder()
                .id(jobId)
                .userId(userId)
                .status("PENDING")
                .createdAt(Instant.now())
                .build();

        when(exportJobRepository.save(any(ExportJob.class))).thenReturn(savedJob);

        ExportStatusResponse response = exportService.requestExport(userId, request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(jobId);
        assertThat(response.status()).isEqualTo("PENDING");
    }

    @Test
    void history_returnsUserExportJobs() {
        ExportJob job = ExportJob.builder()
                .id(jobId)
                .userId(userId)
                .status("COMPLETED")
                .createdAt(Instant.now())
                .build();

        when(exportJobRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(job));

        List<ExportStatusResponse> history = exportService.history(userId);

        assertThat(history).hasSize(1);
        assertThat(history.get(0).status()).isEqualTo("COMPLETED");
    }
}
