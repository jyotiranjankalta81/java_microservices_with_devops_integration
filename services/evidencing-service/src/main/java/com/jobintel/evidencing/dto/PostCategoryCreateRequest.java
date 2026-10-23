package com.jobintel.evidencing.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostCategoryCreateRequest {

    @NotBlank(message = "Category name is required")
    private String name;

    private String description;
    private String icon;
    private int displayOrder;
}
