package com.jobintel.job.dto;

import com.jobintel.job.domain.enums.EmploymentType;
import com.jobintel.job.domain.enums.JobStatus;
import com.jobintel.job.domain.enums.WorkMode;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

@Builder
public record JobCreateRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 255) String company,
        @NotBlank @Size(max = 255) String role,
        String description,
        @NotEmpty Set<@NotBlank @Size(max = 100) String> skills,
        @NotEmpty Set<@NotBlank @Size(max = 100) String> technologies,
        @NotBlank @Size(max = 100) String country,
        @Size(max = 100) String state,
        @NotBlank @Size(max = 100) String city,
        String location,
        @NotNull WorkMode workMode,
        String workModeNotes,
        @Min(0) Integer experienceMin,
        @Min(0) Integer experienceMax,
        @DecimalMin("0") BigDecimal salaryMin,
        @DecimalMin("0") BigDecimal salaryMax,
        @Size(min = 3, max = 3) String salaryCurrency,
        @NotNull EmploymentType employmentType,
        String jobSource,
        @Size(max = 1000) String jobUrl,
        @Size(max = 1000) String applyUrl,
        LocalDate postedDate,
        LocalDate applicationDeadline,
        JobStatus status,
        String notes,
        Set<@NotBlank @Size(max = 100) String> tags
) {}
