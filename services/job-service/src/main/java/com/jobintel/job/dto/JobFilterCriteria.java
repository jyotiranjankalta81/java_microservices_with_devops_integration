package com.jobintel.job.dto;

import com.jobintel.job.domain.enums.EmploymentType;
import com.jobintel.job.domain.enums.JobStatus;
import com.jobintel.job.domain.enums.WorkMode;
import lombok.Builder;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Builder
public record JobFilterCriteria(
        String search,
        String company,
        String country,
        String city,
        String role,
        String skill,
        String technology,
        WorkMode workMode,
        Integer experienceMin,
        Integer experienceMax,
        String source,
        JobStatus status,
        EmploymentType employmentType,
        Boolean global,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate postedFrom,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate postedTo,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdFrom,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdTo
) {}
