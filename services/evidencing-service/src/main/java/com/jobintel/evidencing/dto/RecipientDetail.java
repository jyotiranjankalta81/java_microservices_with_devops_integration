package com.jobintel.evidencing.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipientDetail {
    private UUID userId;
    private String displayName;
    private String avatarUrl;
    private String role;
}
