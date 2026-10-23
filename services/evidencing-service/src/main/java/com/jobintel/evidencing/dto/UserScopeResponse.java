package com.jobintel.evidencing.dto;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserScopeResponse {
    private UUID userId;
    private String displayName;
    private String role;
    private String avatarUrl;
    private UUID schoolId;
    private String schoolName;
    private UUID branchId;
    private String branchName;
    private UUID classId;
    private String className;
    private List<String> permissions;
}
