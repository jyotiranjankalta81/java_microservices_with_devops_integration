package com.jobintel.job.repository;

import com.jobintel.job.domain.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {

    Optional<Job> findByIdAndUserId(Long id, UUID userId);

    boolean existsByUserIdAndJobUrl(UUID userId, String jobUrl);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT j.role FROM Job j WHERE j.role IS NOT NULL AND j.role != '' ORDER BY j.role ASC")
    java.util.List<String> findDistinctRoles();
}
