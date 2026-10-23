package com.jobintel.evidencing.repository;

import com.jobintel.evidencing.domain.entity.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SchoolRepository extends JpaRepository<School, UUID> {
    Optional<School> findByCode(String code);
}
