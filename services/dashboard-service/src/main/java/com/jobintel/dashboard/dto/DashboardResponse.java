package com.jobintel.dashboard.dto;

import java.util.List;
import java.util.Map;

public record DashboardResponse(
        long totalJobs,
        long jobsToday,
        long jobsThisWeek,
        long jobsThisMonth,
        Map<String, Long> byCompany,
        Map<String, Long> byCountry,
        Map<String, Long> byCity,
        Map<String, Long> byRole,
        Map<String, Long> byTechnology,
        Map<String, Long> bySkill,
        Map<String, Long> byWorkMode,
        Map<String, Long> bySource,
        Map<String, Long> byExperience
) {}
