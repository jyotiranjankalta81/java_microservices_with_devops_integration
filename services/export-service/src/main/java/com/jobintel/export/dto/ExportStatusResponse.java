package com.jobintel.export.dto;

import java.time.Instant;
import java.util.UUID;

public record ExportStatusResponse(
        UUID id, String status, String filePath, String errorMessage,
        Instant createdAt, Instant completedAt
) {}
