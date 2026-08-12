package com.jobintel.export.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobintel.common.events.EventTypes;
import com.jobintel.common.events.KafkaTopics;
import com.jobintel.export.domain.ExportJob;
import com.jobintel.export.dto.ExportRequest;
import com.jobintel.export.dto.ExportStatusResponse;
import com.jobintel.export.repository.ExportJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final ExportJobRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public ExportStatusResponse requestExport(UUID userId, ExportRequest request) throws Exception {
        ExportJob job = ExportJob.builder()
                .userId(userId)
                .status("PENDING")
                .filterJson(objectMapper.writeValueAsString(request))
                .createdAt(Instant.now())
                .build();
        ExportJob saved = repository.save(job);
        kafkaTemplate.send(KafkaTopics.EXPORT_REQUESTED, saved.getId().toString(),
                objectMapper.writeValueAsString(saved));
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ExportStatusResponse> history(UUID userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ExportStatusResponse status(UUID userId, UUID exportId) {
        ExportJob job = repository.findById(exportId)
                .filter(j -> j.getUserId().equals(userId))
                .orElseThrow();
        return toResponse(job);
    }

    private ExportStatusResponse toResponse(ExportJob job) {
        return new ExportStatusResponse(job.getId(), job.getStatus(), job.getFilePath(),
                job.getErrorMessage(), job.getCreatedAt(), job.getCompletedAt());
    }
}
