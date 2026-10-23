package com.jobintel.evidencing.dto;

import com.jobintel.evidencing.domain.enums.MediaType;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaDetail {
    private UUID mediaId;
    private MediaType mediaType;
    private String url;
    private String thumbnailUrl;
    private Integer width;
    private Integer height;
    private Double durationSeconds;
    private int displayOrder;
}
