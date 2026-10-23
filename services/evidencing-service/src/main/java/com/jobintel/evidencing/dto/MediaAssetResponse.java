package com.jobintel.evidencing.dto;

import com.jobintel.evidencing.domain.enums.MediaAssetStatus;
import com.jobintel.evidencing.domain.enums.MediaType;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaAssetResponse {
    private UUID mediaAssetId;
    private String originalFilename;
    private MediaType mediaType;
    private MediaAssetStatus status;
    private String url;
    private String thumbnailUrl;
    private Integer width;
    private Integer height;
    private Double durationSeconds;
    private String errorMessage;
}
