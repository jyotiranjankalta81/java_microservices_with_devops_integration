package com.jobintel.evidencing.repository;

import com.jobintel.evidencing.domain.entity.UserScope;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserScopeRepository extends JpaRepository<UserScope, UUID> {

    @EntityGraph(attributePaths = {"school", "branch", "schoolClass"})
    Optional<UserScope> findByUserId(UUID userId);

    @EntityGraph(attributePaths = {"school", "branch", "schoolClass"})
    List<UserScope> findByBranchId(UUID branchId);

    @EntityGraph(attributePaths = {"school", "branch", "schoolClass"})
    List<UserScope> findBySchoolId(UUID schoolId);

    @EntityGraph(attributePaths = {"school", "branch", "schoolClass"})
    List<UserScope> findByUserIdIn(Collection<UUID> userIds);

    @Override
    @EntityGraph(attributePaths = {"school", "branch", "schoolClass"})
    List<UserScope> findAll();
}
