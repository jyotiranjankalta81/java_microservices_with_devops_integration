package com.jobintel.evidencing.dto;

import com.jobintel.evidencing.domain.enums.PostVisibility;
import jakarta.validation.constraints.NotEmpty;
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
public class PostCreateRequest {

    @NotNull(message = "School context is required")
    private UUID schoolId;

    @NotNull(message = "Branch context is required")
    private UUID branchId;

    private UUID classId;

    @NotNull(message = "Category / Domain is required")
    private UUID categoryId;

    @Size(max = 4000, message = "Caption must not exceed 4000 characters")
    private String caption;

    @NotNull(message = "Visibility (PUBLIC or PRIVATE) is required")
    private PostVisibility visibility;

    private List<UUID> taggedRecipientIds;

    @NotEmpty(message = "At least one media asset (image or video) is required")
    private List<UUID> mediaAssetIds;

    private String idempotencyKey;
}
