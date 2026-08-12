package com.jobintel.dashboard.repository;

import com.jobintel.dashboard.domain.DashboardSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface DashboardSummaryRepository extends JpaRepository<DashboardSummary, UUID> {}
