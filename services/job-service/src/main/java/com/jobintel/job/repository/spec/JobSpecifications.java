package com.jobintel.job.repository.spec;

import com.jobintel.job.domain.entity.*;
import com.jobintel.job.dto.JobFilterCriteria;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class JobSpecifications {

    private JobSpecifications() {}

    public static Specification<Job> withFilters(UUID userId, JobFilterCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (userId != null) {
                predicates.add(cb.equal(root.get("userId"), userId));
            }

            if (criteria == null) {
                return cb.and(predicates.toArray(new Predicate[0]));
            }

            if (StringUtils.hasText(criteria.search())) {
                String pattern = "%" + criteria.search().toLowerCase() + "%";
                Join<Job, Company> company = root.join("company", JoinType.LEFT);
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("role")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern),
                        cb.like(cb.lower(company.get("name")), pattern)
                ));
            }

            if (StringUtils.hasText(criteria.company())) {
                Join<Job, Company> company = root.join("company");
                predicates.add(cb.equal(cb.lower(company.get("name")), criteria.company().toLowerCase()));
            }

            if (StringUtils.hasText(criteria.country()) || StringUtils.hasText(criteria.city())) {
                Join<Job, Location> location = root.join("location", JoinType.INNER);
                if (StringUtils.hasText(criteria.country())) {
                    predicates.add(cb.equal(cb.lower(location.get("country")), criteria.country().toLowerCase()));
                }
                if (StringUtils.hasText(criteria.city())) {
                    predicates.add(cb.equal(cb.lower(location.get("city")), criteria.city().toLowerCase()));
                }
            }

            if (StringUtils.hasText(criteria.role())) {
                predicates.add(cb.like(cb.lower(root.get("role")), "%" + criteria.role().toLowerCase() + "%"));
            }

            if (criteria.workMode() != null) {
                predicates.add(cb.equal(root.get("workMode"), criteria.workMode()));
            }

            if (criteria.status() != null) {
                predicates.add(cb.equal(root.get("status"), criteria.status()));
            }

            if (criteria.employmentType() != null) {
                predicates.add(cb.equal(root.get("employmentType"), criteria.employmentType()));
            }

            if (criteria.experienceMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("experienceMax"), criteria.experienceMin()));
            }
            if (criteria.experienceMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("experienceMin"), criteria.experienceMax()));
            }

            if (criteria.postedFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("postedDate"), criteria.postedFrom()));
            }
            if (criteria.postedTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("postedDate"), criteria.postedTo()));
            }

            if (criteria.createdFrom() != null) {
                Instant from = criteria.createdFrom().atStartOfDay().toInstant(ZoneOffset.UTC);
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            }
            if (criteria.createdTo() != null) {
                Instant to = criteria.createdTo().plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
                predicates.add(cb.lessThan(root.get("createdAt"), to));
            }

            if (StringUtils.hasText(criteria.skill())) {
                Join<Job, Skill> skills = root.join("skills");
                predicates.add(cb.equal(cb.lower(skills.get("name")), criteria.skill().toLowerCase()));
                query.distinct(true);
            }

            if (StringUtils.hasText(criteria.technology())) {
                Join<Job, Technology> techs = root.join("technologies");
                predicates.add(cb.equal(cb.lower(techs.get("name")), criteria.technology().toLowerCase()));
                query.distinct(true);
            }

            if (StringUtils.hasText(criteria.source())) {
                Join<Job, JobSource> source = root.join("jobSource", JoinType.INNER);
                predicates.add(cb.equal(cb.lower(source.get("name")), criteria.source().toLowerCase()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
