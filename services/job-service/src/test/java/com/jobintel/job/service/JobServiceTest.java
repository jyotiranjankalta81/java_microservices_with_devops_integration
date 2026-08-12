package com.jobintel.job.service;

import com.jobintel.common.exception.DuplicateResourceException;
import com.jobintel.job.domain.entity.Job;
import com.jobintel.job.domain.enums.EmploymentType;
import com.jobintel.job.domain.enums.JobStatus;
import com.jobintel.job.domain.enums.WorkMode;
import com.jobintel.job.dto.JobCreateRequest;
import com.jobintel.job.dto.JobResponse;
import com.jobintel.job.mapper.JobMapper;
import com.jobintel.job.repository.CompanyRepository;
import com.jobintel.job.repository.JobRepository;
import com.jobintel.job.repository.JobSourceRepository;
import com.jobintel.job.repository.SkillRepository;
import com.jobintel.job.repository.TechnologyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock JobRepository jobRepository;
    @Mock CompanyRepository companyRepository;
    @Mock SkillRepository skillRepository;
    @Mock TechnologyRepository technologyRepository;
    @Mock JobSourceRepository jobSourceRepository;
    @Mock OutboxService outboxService;
    @Mock JobMapper jobMapper;
    @Mock com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @InjectMocks JobService jobService;

    @Test
    void createJob_rejectsDuplicateUrl() {
        UUID userId = UUID.randomUUID();
        JobCreateRequest request = JobCreateRequest.builder()
                .title("Engineer").company("Acme").role("Backend")
                .skills(Set.of("Java")).technologies(Set.of("Spring"))
                .country("USA").city("NYC").workMode(WorkMode.REMOTE)
                .employmentType(EmploymentType.FULL_TIME)
                .jobUrl("https://example.com/job/1")
                .build();

        when(jobRepository.existsByUserIdAndJobUrl(userId, "https://example.com/job/1")).thenReturn(true);

        assertThatThrownBy(() -> jobService.createJob(userId, request))
                .isInstanceOf(DuplicateResourceException.class);
        verify(jobRepository, never()).save(any());
    }
}
