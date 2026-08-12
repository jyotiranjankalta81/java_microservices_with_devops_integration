package com.jobintel.job.repository;

import com.jobintel.job.domain.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByNameIgnoreCase(String name);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT c.name FROM Company c WHERE c.name IS NOT NULL AND c.name != '' ORDER BY c.name ASC")
    java.util.List<String> findDistinctCompanyNames();
}
