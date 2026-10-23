package com.jobintel.evidencing.dto;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponse {
    private UUID id;
    private UUID postId;
    private UUID userId;
    private String authorName;
    private String authorAvatarUrl;
    private String authorRole;
    private String commentText;
    private Instant createdAt;
}
