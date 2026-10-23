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
public class MediaInitiateResponse {
    private UUID mediaAssetId;
    private String uploadUrl;
    private String storageKey;
    private MediaType mediaType;
    private MediaAssetStatus status;
}
