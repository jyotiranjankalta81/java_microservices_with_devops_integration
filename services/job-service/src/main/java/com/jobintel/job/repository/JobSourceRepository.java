package com.jobintel.job.repository;

import com.jobintel.job.domain.entity.JobSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobSourceRepository extends JpaRepository<JobSource, Long> {
    Optional<JobSource> findByNameIgnoreCase(String name);
}
