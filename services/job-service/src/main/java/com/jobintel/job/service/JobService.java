package com.jobintel.job.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobintel.common.events.EventTypes;
import com.jobintel.common.exception.DuplicateResourceException;
import com.jobintel.common.exception.ResourceNotFoundException;
import com.jobintel.common.web.CorrelationIdFilter;
import com.jobintel.job.domain.entity.*;
import com.jobintel.job.domain.enums.JobStatus;
import com.jobintel.job.dto.*;
import com.jobintel.job.mapper.JobMapper;
import com.jobintel.job.repository.*;
import com.jobintel.job.repository.spec.JobSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final LocationRepository locationRepository;
    private final SkillRepository skillRepository;
    private final TechnologyRepository technologyRepository;
    private final JobSourceRepository jobSourceRepository;
    private final OutboxService outboxService;
    private final JobMapper jobMapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public JobResponse createJob(UUID userId, JobCreateRequest request) {
        if (StringUtils.hasText(request.jobUrl()) && jobRepository.existsByUserIdAndJobUrl(userId, request.jobUrl())) {
            throw new DuplicateResourceException("Job with this URL already exists for user");
        }

        Job job = buildJobFromRequest(userId, request);
        Job saved = jobRepository.save(job);
        publishOutboxEvent(saved, EventTypes.JOB_CREATED);
        log.info("Created job id={} userId={}", saved.getId(), userId);
        return jobMapper.toResponse(saved);
    }

    @Cacheable(value = "jobsCache", key = "#jobId + '-' + #userId", unless = "#result == null")
    @Transactional(readOnly = true)
    public JobResponse getJob(UUID userId, Long jobId) {
        Job job = findJobForUser(userId, jobId);
        return jobMapper.toResponse(job);
    }

    @Transactional(readOnly = true)
    public Page<JobResponse> searchJobs(UUID userId, JobFilterCriteria criteria, Pageable pageable) {
        if (criteria != null && Boolean.TRUE.equals(criteria.global())) {
            Specification<Job> globalSpec = JobSpecifications.withFilters(null, criteria);
            return jobRepository.findAll(globalSpec, pageable).map(jobMapper::toResponse);
        }
        Specification<Job> spec = JobSpecifications.withFilters(userId, criteria);
        Page<JobResponse> result = jobRepository.findAll(spec, pageable).map(jobMapper::toResponse);
        if (result.isEmpty() && (criteria == null || criteria.search() == null)) {
            Specification<Job> globalSpec = JobSpecifications.withFilters(null, criteria);
            return jobRepository.findAll(globalSpec, pageable).map(jobMapper::toResponse);
        }
        return result;
    }

    @CacheEvict(value = "jobsCache", key = "#jobId + '-' + #userId")
    @Transactional
    public JobResponse updateJob(UUID userId, Long jobId, JobUpdateRequest request) {
        Job job = findJobForUser(userId, jobId);
        if (!job.getVersion().equals(request.version())) {
            throw new ObjectOptimisticLockingFailureException(Job.class, jobId);
        }
        applyUpdate(job, request);
        try {
            Job saved = jobRepository.save(job);
            publishOutboxEvent(saved, EventTypes.JOB_UPDATED);
            return jobMapper.toResponse(saved);
        } catch (ObjectOptimisticLockingFailureException ex) {
            throw ex;
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateResourceException("Job update violates uniqueness constraint");
        }
    }

    @CacheEvict(value = "jobsCache", key = "#jobId + '-' + #userId")
    @Transactional
    public void deleteJob(UUID userId, Long jobId) {
        Job job = findJobForUser(userId, jobId);
        jobRepository.delete(job);
        publishOutboxEvent(job, EventTypes.JOB_DELETED);
        log.info("Deleted job id={} userId={}", jobId, userId);
    }

    private Job findJobForUser(UUID userId, Long jobId) {
        return jobRepository.findByIdAndUserId(jobId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", jobId));
    }

    private Job buildJobFromRequest(UUID userId, JobCreateRequest request) {
        return Job.builder()
                .userId(userId)
                .title(request.title())
                .company(resolveCompany(request.company()))
                .role(request.role())
                .description(request.description())
                .location(resolveLocation(request.country(), request.state(), request.city(), request.location()))
                .workMode(request.workMode())
                .workModeNotes(request.workModeNotes())
                .experienceMin(request.experienceMin())
                .experienceMax(request.experienceMax())
                .salaryMin(request.salaryMin())
                .salaryMax(request.salaryMax())
                .salaryCurrency(request.salaryCurrency() != null ? request.salaryCurrency() : "USD")
                .employmentType(request.employmentType())
                .jobSource(resolveSource(request.jobSource()))
                .jobUrl(request.jobUrl())
                .applyUrl(request.applyUrl())
                .postedDate(request.postedDate())
                .applicationDeadline(request.applicationDeadline())
                .status(request.status() != null ? request.status() : JobStatus.ACTIVE)
                .notes(request.notes())
                .tags(request.tags() != null ? new HashSet<>(request.tags()) : new HashSet<>())
                .skills(resolveSkills(request.skills()))
                .technologies(resolveTechnologies(request.technologies()))
                .build();
    }

    private void applyUpdate(Job job, JobUpdateRequest request) {
        job.setTitle(request.title());
        job.setCompany(resolveCompany(request.company()));
        job.setRole(request.role());
        job.setDescription(request.description());
        job.setLocation(resolveLocation(request.country(), request.state(), request.city(), request.location()));
        job.setWorkMode(request.workMode());
        job.setWorkModeNotes(request.workModeNotes());
        job.setExperienceMin(request.experienceMin());
        job.setExperienceMax(request.experienceMax());
        job.setSalaryMin(request.salaryMin());
        job.setSalaryMax(request.salaryMax());
        job.setSalaryCurrency(request.salaryCurrency());
        job.setEmploymentType(request.employmentType());
        job.setJobSource(resolveSource(request.jobSource()));
        job.setJobUrl(request.jobUrl());
        job.setApplyUrl(request.applyUrl());
        job.setPostedDate(request.postedDate());
        job.setApplicationDeadline(request.applicationDeadline());
        job.setStatus(request.status());
        job.setNotes(request.notes());
        if (request.tags() != null) {
            job.setTags(new HashSet<>(request.tags()));
        }
        if (request.skills() != null) {
            job.setSkills(resolveSkills(request.skills()));
        }
        if (request.technologies() != null) {
            job.setTechnologies(resolveTechnologies(request.technologies()));
        }
    }

    @Transactional(readOnly = true)
    public List<String> getCountries() {
        return locationRepository.findDistinctCountries();
    }

    @Transactional(readOnly = true)
    public List<String> getCities(String country) {
        return locationRepository.findDistinctCitiesByCountry(country);
    }

    @Transactional(readOnly = true)
    public List<String> getRoles() {
        return jobRepository.findDistinctRoles();
    }

    @Transactional(readOnly = true)
    public List<String> getCompanies() {
        return companyRepository.findDistinctCompanyNames();
    }

    private Company resolveCompany(String name) {
        return companyRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> companyRepository.save(Company.builder().name(name.trim()).build()));
    }

    private JobSource resolveSource(String name) {
        if (!StringUtils.hasText(name)) return null;
        return jobSourceRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> jobSourceRepository.save(JobSource.builder().name(name.trim()).active(true).build()));
    }

    private Location resolveLocation(String country, String state, String city, String locationText) {
        if (!StringUtils.hasText(country) || !StringUtils.hasText(city)) {
            return null;
        }
        String normalizedCountry = country.trim();
        String normalizedCity = city.trim();
        String normalizedState = StringUtils.hasText(state) ? state.trim() : null;

        Optional<Location> existing = normalizedState == null
                ? locationRepository.findByCountryIgnoreCaseAndCityIgnoreCaseAndStateIsNull(normalizedCountry, normalizedCity)
                : locationRepository.findByCountryIgnoreCaseAndCityIgnoreCaseAndStateIgnoreCase(
                        normalizedCountry, normalizedCity, normalizedState);

        return existing.orElseGet(() -> locationRepository.save(Location.builder()
                .country(normalizedCountry)
                .state(normalizedState)
                .city(normalizedCity)
                .locationText(locationText)
                .build()));
    }

    private Set<Skill> resolveSkills(Set<String> names) {
        Set<Skill> skills = new HashSet<>();
        for (String name : names) {
            skills.add(skillRepository.findByNameIgnoreCase(name)
                    .orElseGet(() -> skillRepository.save(Skill.builder().name(name.trim()).build())));
        }
        return skills;
    }

    private Set<Technology> resolveTechnologies(Set<String> names) {
        Set<Technology> techs = new HashSet<>();
        for (String name : names) {
            techs.add(technologyRepository.findByNameIgnoreCase(name)
                    .orElseGet(() -> technologyRepository.save(Technology.builder().name(name.trim()).build())));
        }
        return techs;
    }

    private void publishOutboxEvent(Job job, String eventType) {
        try {
            JobResponse payload = jobMapper.toResponse(job);
            String json = objectMapper.writeValueAsString(payload);
            outboxService.saveEvent("Job", String.valueOf(job.getId()), eventType, json,
                    CorrelationIdFilter.getCorrelationId());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize job event", e);
        }
    }
}
