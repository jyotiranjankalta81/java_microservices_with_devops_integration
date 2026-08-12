package com.jobintel.job.dto;

import com.jobintel.job.domain.enums.EmploymentType;
import com.jobintel.job.domain.enums.JobStatus;
import com.jobintel.job.domain.enums.WorkMode;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

@Builder
public record JobResponse(
        java.util.UUID userId,
        Long id,
        String title,
        String company,
        String role,
        String description,
        Set<String> skills,
        Set<String> technologies,
        String country,
        String state,
        String city,
        String location,
        WorkMode workMode,
        String workModeNotes,
        Integer experienceMin,
        Integer experienceMax,
        BigDecimal salaryMin,
        BigDecimal salaryMax,
        String salaryCurrency,
        EmploymentType employmentType,
        String jobSource,
        String jobUrl,
        String applyUrl,
        LocalDate postedDate,
        LocalDate applicationDeadline,
        JobStatus status,
        String notes,
        Set<String> tags,
        Long version,
        Instant createdAt,
        Instant updatedAt
) {}
