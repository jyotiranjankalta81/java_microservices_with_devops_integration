package com.jobintel.dashboard.repository;

import com.jobintel.dashboard.domain.DimensionCount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DimensionCountRepository extends JpaRepository<DimensionCount, Long> {
    List<DimensionCount> findByUserId(UUID userId);
    Optional<DimensionCount> findByUserIdAndDimensionAndDimensionValue(UUID userId, String dimension, String value);
}
