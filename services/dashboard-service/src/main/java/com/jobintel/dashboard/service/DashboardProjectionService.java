package com.jobintel.dashboard.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.jobintel.dashboard.domain.DimensionCount;
import com.jobintel.dashboard.domain.ProcessedEvent;
import com.jobintel.dashboard.domain.DashboardSummary;
import com.jobintel.dashboard.dto.DashboardResponse;
import com.jobintel.dashboard.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardProjectionService {

    private final DashboardSummaryRepository summaryRepository;
    private final DimensionCountRepository dimensionRepository;
    private final ProcessedEventRepository processedEventRepository;

    public boolean isProcessed(UUID eventId) {
        return processedEventRepository.existsById(eventId);
    }

    @Transactional
    public void processEvent(String eventType, JsonNode payload, UUID eventId) {
        UUID userId = UUID.fromString(payload.get("userId").asText());

        if ("job.deleted".equals(eventType)) {
            decrementSummary(userId);
        } else {
            upsertSummary(userId, payload);
            upsertDimensions(userId, payload);
        }
        processedEventRepository.save(ProcessedEvent.builder().eventId(eventId).build());
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(UUID userId) {
        DashboardSummary summary = summaryRepository.findById(userId)
                .orElse(DashboardSummary.empty(userId));
        List<DimensionCount> dims = dimensionRepository.findByUserId(userId);
        Map<String, Map<String, Long>> grouped = dims.stream()
                .collect(Collectors.groupingBy(DimensionCount::getDimension,
                        Collectors.toMap(DimensionCount::getDimensionValue, DimensionCount::getCount, Long::sum)));

        return new DashboardResponse(
                summary.getTotalJobs(), summary.getJobsToday(), summary.getJobsThisWeek(), summary.getJobsThisMonth(),
                grouped.getOrDefault("company", Map.of()),
                grouped.getOrDefault("country", Map.of()),
                grouped.getOrDefault("city", Map.of()),
                grouped.getOrDefault("role", Map.of()),
                grouped.getOrDefault("technology", Map.of()),
                grouped.getOrDefault("skill", Map.of()),
                grouped.getOrDefault("workMode", Map.of()),
                grouped.getOrDefault("source", Map.of()),
                grouped.getOrDefault("experience", Map.of())
        );
    }

    @Transactional(readOnly = true)
    public DashboardResponse getGlobalDashboard() {
        List<DashboardSummary> summaries = summaryRepository.findAll();
        long totalJobs = summaries.stream().mapToLong(DashboardSummary::getTotalJobs).sum();
        long jobsToday = summaries.stream().mapToLong(DashboardSummary::getJobsToday).sum();
        long jobsThisWeek = summaries.stream().mapToLong(DashboardSummary::getJobsThisWeek).sum();
        long jobsThisMonth = summaries.stream().mapToLong(DashboardSummary::getJobsThisMonth).sum();

        List<DimensionCount> dims = dimensionRepository.findAll();
        Map<String, Map<String, Long>> grouped = dims.stream()
                .collect(Collectors.groupingBy(DimensionCount::getDimension,
                        Collectors.toMap(DimensionCount::getDimensionValue, DimensionCount::getCount, Long::sum)));

        return new DashboardResponse(
                totalJobs, jobsToday, jobsThisWeek, jobsThisMonth,
                grouped.getOrDefault("company", Map.of()),
                grouped.getOrDefault("country", Map.of()),
                grouped.getOrDefault("city", Map.of()),
                grouped.getOrDefault("role", Map.of()),
                grouped.getOrDefault("technology", Map.of()),
                grouped.getOrDefault("skill", Map.of()),
                grouped.getOrDefault("workMode", Map.of()),
                grouped.getOrDefault("source", Map.of()),
                grouped.getOrDefault("experience", Map.of())
        );
    }

    private void upsertSummary(UUID userId, JsonNode payload) {
        DashboardSummary s = summaryRepository.findById(userId).orElse(DashboardSummary.empty(userId));
        s.setTotalJobs(s.getTotalJobs() + 1);
        Instant created = payload.has("createdAt") ? Instant.parse(payload.get("createdAt").asText()) : Instant.now();
        LocalDate date = created.atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        if (date.equals(today)) s.setJobsToday(s.getJobsToday() + 1);
        if (date.isAfter(today.minusWeeks(1))) s.setJobsThisWeek(s.getJobsThisWeek() + 1);
        if (date.getMonth() == today.getMonth()) s.setJobsThisMonth(s.getJobsThisMonth() + 1);
        summaryRepository.save(s);
    }

    private void decrementSummary(UUID userId) {
        summaryRepository.findById(userId).ifPresent(s -> {
            s.setTotalJobs(Math.max(0, s.getTotalJobs() - 1));
            summaryRepository.save(s);
        });
    }

    private void upsertDimensions(UUID userId, JsonNode payload) {
        incrementDimension(userId, "company", payload.path("company").asText(null));
        incrementDimension(userId, "country", payload.path("country").asText(null));
        incrementDimension(userId, "city", payload.path("city").asText(null));
        incrementDimension(userId, "role", payload.path("role").asText(null));
        incrementDimension(userId, "workMode", payload.path("workMode").asText(null));
        incrementDimension(userId, "source", payload.path("jobSource").asText(null));
        if (payload.has("technologies")) {
            payload.get("technologies").forEach(t -> incrementDimension(userId, "technology", t.asText()));
        }
        if (payload.has("skills")) {
            payload.get("skills").forEach(s -> incrementDimension(userId, "skill", s.asText()));
        }
        String exp = payload.path("experienceMin").asText("0") + "-" + payload.path("experienceMax").asText("0");
        incrementDimension(userId, "experience", exp);
    }

    private void incrementDimension(UUID userId, String dimension, String value) {
        if (value == null || value.isBlank()) return;
        dimensionRepository.findByUserIdAndDimensionAndDimensionValue(userId, dimension, value)
                .ifPresentOrElse(d -> {
                    d.setCount(d.getCount() + 1);
                    dimensionRepository.save(d);
                }, () -> dimensionRepository.save(DimensionCount.builder()
                        .userId(userId).dimension(dimension).dimensionValue(value).count(1L).build()));
    }
}
