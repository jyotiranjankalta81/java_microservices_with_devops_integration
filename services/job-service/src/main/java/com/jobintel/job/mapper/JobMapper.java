package com.jobintel.job.mapper;

import com.jobintel.job.domain.entity.Job;
import com.jobintel.job.dto.JobResponse;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class JobMapper {

    public JobResponse toResponse(Job job) {
        return JobResponse.builder()
                .userId(job.getUserId())
                .id(job.getId())
                .title(job.getTitle())
                .company(job.getCompany().getName())
                .role(job.getRole())
                .description(job.getDescription())
                .skills(job.getSkills().stream().map(s -> s.getName()).collect(Collectors.toSet()))
                .technologies(job.getTechnologies().stream().map(t -> t.getName()).collect(Collectors.toSet()))
                .country(job.getLocation() != null ? job.getLocation().getCountry() : null)
                .state(job.getLocation() != null ? job.getLocation().getState() : null)
                .city(job.getLocation() != null ? job.getLocation().getCity() : null)
                .location(job.getLocation() != null ? job.getLocation().getLocationText() : null)
                .workMode(job.getWorkMode())
                .workModeNotes(job.getWorkModeNotes())
                .experienceMin(job.getExperienceMin())
                .experienceMax(job.getExperienceMax())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .salaryCurrency(job.getSalaryCurrency())
                .employmentType(job.getEmploymentType())
                .jobSource(job.getJobSource() != null ? job.getJobSource().getName() : null)
                .jobUrl(job.getJobUrl())
                .applyUrl(job.getApplyUrl())
                .postedDate(job.getPostedDate())
                .applicationDeadline(job.getApplicationDeadline())
                .status(job.getStatus())
                .notes(job.getNotes())
                .tags(job.getTags())
                .version(job.getVersion())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }
}
