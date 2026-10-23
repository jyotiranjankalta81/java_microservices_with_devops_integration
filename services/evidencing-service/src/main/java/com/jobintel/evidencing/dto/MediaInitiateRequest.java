package com.jobintel.evidencing.dto;

import com.jobintel.evidencing.domain.enums.MediaType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaInitiateRequest {

    @NotBlank(message = "Filename is required")
    private String originalFilename;

    @NotBlank(message = "Content type is required")
    private String contentType;

    @NotNull(message = "File size is required")
    private Long fileSizeBytes;

    @NotNull(message = "Media type is required (IMAGE or VIDEO)")
    private MediaType mediaType;
}
