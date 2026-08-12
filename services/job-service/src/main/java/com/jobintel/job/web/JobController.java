package com.jobintel.job.web;

import com.jobintel.job.dto.JobCreateRequest;
import com.jobintel.job.dto.JobFilterCriteria;
import com.jobintel.job.dto.JobResponse;
import com.jobintel.job.dto.JobUpdateRequest;
import com.jobintel.job.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Tag(name = "Jobs", description = "Job CRUD and search APIs")
public class JobController {

    private final JobService jobService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new job opportunity")
    public JobResponse create(@RequestHeader("X-User-Id") UUID userId,
                              @Valid @RequestBody JobCreateRequest request) {
        return jobService.createJob(userId, request);
    }

    @GetMapping("/{id:\\d+}")
    @Operation(summary = "Get job by ID")
    public JobResponse get(@RequestHeader("X-User-Id") UUID userId, @PathVariable Long id) {
        return jobService.getJob(userId, id);
    }

    @GetMapping
    @Operation(summary = "Search, filter, sort and paginate jobs")
    public Page<JobResponse> search(
            @RequestHeader("X-User-Id") UUID userId,
            JobFilterCriteria criteria,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return jobService.searchJobs(userId, criteria, pageable);
    }

    @PutMapping("/{id:\\d+}")
    @Operation(summary = "Update job (optimistic locking via version)")
    public JobResponse update(@RequestHeader("X-User-Id") UUID userId,
                              @PathVariable Long id,
                              @Valid @RequestBody JobUpdateRequest request) {
        return jobService.updateJob(userId, id, request);
    }

    @DeleteMapping("/{id:\\d+}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete job")
    public void delete(@RequestHeader("X-User-Id") UUID userId, @PathVariable Long id) {
        jobService.deleteJob(userId, id);
    }

    @GetMapping("/countries")
    @Operation(summary = "Get distinct countries")
    public java.util.List<String> getCountries() {
        return jobService.getCountries();
    }

    @GetMapping("/cities")
    @Operation(summary = "Get distinct cities (optionally filtered by country)")
    public java.util.List<String> getCities(@RequestParam(name = "country", required = false) String country) {
        return jobService.getCities(country);
    }

    @GetMapping("/roles")
    @Operation(summary = "Get distinct roles")
    public java.util.List<String> getRoles() {
        return jobService.getRoles();
    }

    @GetMapping("/companies")
    @Operation(summary = "Get distinct companies")
    public java.util.List<String> getCompanies() {
        return jobService.getCompanies();
    }
}
