package com.jobintel.evidencing.dto;

import com.jobintel.evidencing.domain.enums.PublicationStatus;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublishStatusResponse {
    private UUID postId;
    private PublicationStatus publicationStatus;
    private String idempotencyKey;
    private Instant publishedAt;
    private String message;
}
