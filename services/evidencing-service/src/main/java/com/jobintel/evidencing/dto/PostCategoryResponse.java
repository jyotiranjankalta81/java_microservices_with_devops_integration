package com.jobintel.evidencing.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostCategoryResponse {
    private UUID id;
    private String name;
    private String description;
    private String icon;
    private boolean active;
    private int displayOrder;
}
