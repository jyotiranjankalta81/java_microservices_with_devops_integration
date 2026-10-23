package com.jobintel.evidencing.dto;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private UUID id;
    private UUID postId;
    private UUID creatorId;
    private String creatorName;
    private String message;
    private boolean read;
    private String link;
    private Instant createdAt;
}
