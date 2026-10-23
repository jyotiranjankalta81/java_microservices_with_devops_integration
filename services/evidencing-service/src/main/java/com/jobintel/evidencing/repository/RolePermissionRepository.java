package com.jobintel.evidencing.repository;

import com.jobintel.evidencing.domain.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, UUID> {

    List<RolePermission> findByRoleIn(Collection<String> roles);

    boolean existsByRoleInAndPermission(Collection<String> roles, String permission);
}
