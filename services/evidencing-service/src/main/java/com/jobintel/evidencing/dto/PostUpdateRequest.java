package com.jobintel.evidencing.dto;

import com.jobintel.evidencing.domain.enums.PostVisibility;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostUpdateRequest {

    @NotNull(message = "Category is required")
    private UUID categoryId;

    @Size(max = 4000, message = "Caption must not exceed 4000 characters")
    private String caption;

    @NotNull(message = "Visibility is required")
    private PostVisibility visibility;

    private List<UUID> taggedRecipientIds;

    private List<UUID> mediaAssetIds;

    @NotNull(message = "Version is required for optimistic locking")
    private Long version;
}
