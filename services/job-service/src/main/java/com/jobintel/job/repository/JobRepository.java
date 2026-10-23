package com.jobintel.job.repository;

import com.jobintel.job.domain.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {

    @EntityGraph(attributePaths = {"company", "location", "jobSource", "tags", "skills", "technologies"})
    Optional<Job> findByIdAndUserId(Long id, UUID userId);

    @Override
    @EntityGraph(attributePaths = {"company", "location", "jobSource", "tags", "skills", "technologies"})
    Page<Job> findAll(Specification<Job> spec, Pageable pageable);

    boolean existsByUserIdAndJobUrl(UUID userId, String jobUrl);

    @Query("SELECT DISTINCT j.role FROM Job j WHERE j.role IS NOT NULL AND j.role != '' ORDER BY j.role ASC")
    List<String> findDistinctRoles();
}
