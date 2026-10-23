package com.jobintel.evidencing.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EligibleRecipientResponse {
    private UUID userId;
    private String displayName;
    private String avatarUrl;
    private String role;
    private UUID schoolId;
    private UUID branchId;
    private UUID classId;
    private String className;
}
