package com.jobintel.evidencing.dto;

import com.jobintel.evidencing.domain.enums.PostVisibility;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostFilterCriteria {
    private UUID categoryId;
    private UUID authorId;
    private PostVisibility visibility;
    private UUID classId;
    private UUID branchId;
    private String search;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant fromDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant toDate;
}
