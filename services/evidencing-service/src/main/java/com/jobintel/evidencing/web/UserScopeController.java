package com.jobintel.evidencing.web;

import com.jobintel.evidencing.domain.entity.Branch;
import com.jobintel.evidencing.domain.entity.School;
import com.jobintel.evidencing.domain.entity.SchoolClass;
import com.jobintel.evidencing.domain.entity.UserScope;
import com.jobintel.evidencing.domain.enums.EvidencingPermission;
import com.jobintel.evidencing.dto.EligibleRecipientResponse;
import com.jobintel.evidencing.dto.UserScopeResponse;
import com.jobintel.evidencing.repository.BranchRepository;
import com.jobintel.evidencing.repository.SchoolClassRepository;
import com.jobintel.evidencing.repository.SchoolRepository;
import com.jobintel.evidencing.repository.UserScopeRepository;
import com.jobintel.evidencing.service.EvidencingAccessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/evidencing")
@RequiredArgsConstructor
@org.springframework.transaction.annotation.Transactional(readOnly = true)
@Tag(name = "Evidencing Scope & Users", description = "Endpoints for user scope profiles, eligible recipient tagging, and school/branch hierarchy")
public class UserScopeController {

    private final UserScopeRepository userScopeRepository;
    private final SchoolRepository schoolRepository;
    private final BranchRepository branchRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final EvidencingAccessService accessService;
    private final UserContextHelper userContextHelper;

    @GetMapping("/users/me")
    @Operation(summary = "Get current user profile, school/branch/class scope, and module permissions")
    public UserScopeResponse getCurrentUserScope(HttpServletRequest httpRequest) {
        UUID userId = userContextHelper.getUserId(httpRequest);
        List<String> roles = userContextHelper.getUserRoles(httpRequest);

        UserScope scope = userScopeRepository.findByUserId(userId)
                .orElse(null);

        Set<String> effectiveRoles = new LinkedHashSet<>(roles);
        if (scope != null && scope.getRole() != null) {
            effectiveRoles.add(scope.getRole().toUpperCase());
        }
        List<String> permissions = accessService.getUserPermissions(effectiveRoles);

        if (scope == null) {
            School defaultSchool = schoolRepository.findAll().stream().findFirst().orElse(null);
            Branch defaultBranch = branchRepository.findAll().stream().findFirst().orElse(null);

            return UserScopeResponse.builder()
                    .userId(userId)
                    .displayName("Staff Member")
                    .role(!effectiveRoles.isEmpty() ? effectiveRoles.iterator().next() : "USER")
                    .schoolId(defaultSchool != null ? defaultSchool.getId() : null)
                    .schoolName(defaultSchool != null ? defaultSchool.getName() : "Oakridge Academy")
                    .branchId(defaultBranch != null ? defaultBranch.getId() : null)
                    .branchName(defaultBranch != null ? defaultBranch.getName() : "Oakridge North Campus")
                    .permissions(permissions)
                    .build();
        }

        return UserScopeResponse.builder()
                .userId(scope.getUserId())
                .displayName(scope.getDisplayName())
                .role(scope.getRole())
                .avatarUrl(scope.getAvatarUrl())
                .schoolId(scope.getSchool() != null ? scope.getSchool().getId() : null)
                .schoolName(scope.getSchool() != null ? scope.getSchool().getName() : null)
                .branchId(scope.getBranch() != null ? scope.getBranch().getId() : null)
                .branchName(scope.getBranch() != null ? scope.getBranch().getName() : null)
                .classId(scope.getSchoolClass() != null ? scope.getSchoolClass().getId() : null)
                .className(scope.getSchoolClass() != null ? scope.getSchoolClass().getName() : null)
                .permissions(permissions)
                .build();
    }

    @GetMapping("/users/all")
    @Operation(summary = "Get all staff profiles with permissions for simulation and testing")
    public List<UserScopeResponse> getAllUsers() {
        return userScopeRepository.findAll().stream().map(scope -> {
            List<String> permissions = accessService.getUserPermissions(List.of(scope.getRole()));
            return UserScopeResponse.builder()
                    .userId(scope.getUserId())
                    .displayName(scope.getDisplayName())
                    .role(scope.getRole())
                    .avatarUrl(scope.getAvatarUrl())
                    .schoolId(scope.getSchool() != null ? scope.getSchool().getId() : null)
                    .schoolName(scope.getSchool() != null ? scope.getSchool().getName() : null)
                    .branchId(scope.getBranch() != null ? scope.getBranch().getId() : null)
                    .branchName(scope.getBranch() != null ? scope.getBranch().getName() : null)
                    .classId(scope.getSchoolClass() != null ? scope.getSchoolClass().getId() : null)
                    .className(scope.getSchoolClass() != null ? scope.getSchoolClass().getName() : null)
                    .permissions(permissions)
                    .build();
        }).collect(Collectors.toList());
    }

    @GetMapping("/recipients/eligible")
    @Operation(summary = "Search eligible recipients for tagging in a private post (same school/branch with EVIDENCING_VIEW permission)")
    public List<EligibleRecipientResponse> getEligibleRecipients(
            @RequestParam(name = "branchId", required = false) UUID branchId,
            @RequestParam(name = "query", required = false) String query,
            HttpServletRequest httpRequest) {

        UUID currentUserId = userContextHelper.getUserId(httpRequest);
        UserScope currentScope = accessService.findUserScope(currentUserId).orElse(null);

        UUID targetBranchId = branchId;
        if (targetBranchId == null && currentScope != null) {
            targetBranchId = currentScope.getBranch().getId();
        }

        if (targetBranchId == null) {
            return List.of();
        }

        List<UserScope> branchStaff = userScopeRepository.findByBranchId(targetBranchId);

        String q = query != null ? query.toLowerCase().trim() : null;

        return branchStaff.stream()
                // Filter out current user from tagging themselves
                .filter(s -> !s.getUserId().equals(currentUserId))
                // Must have EVIDENCING_VIEW permission! (Excludes nanny, peon, driver!)
                .filter(s -> accessService.hasPermission(List.of(s.getRole()), EvidencingPermission.EVIDENCING_VIEW.name()))
                // Optional search filter
                .filter(s -> q == null || s.getDisplayName().toLowerCase().contains(q) || s.getRole().toLowerCase().contains(q))
                .map(s -> EligibleRecipientResponse.builder()
                        .userId(s.getUserId())
                        .displayName(s.getDisplayName())
                        .avatarUrl(s.getAvatarUrl())
                        .role(s.getRole())
                        .schoolId(s.getSchool() != null ? s.getSchool().getId() : null)
                        .branchId(s.getBranch() != null ? s.getBranch().getId() : null)
                        .classId(s.getSchoolClass() != null ? s.getSchoolClass().getId() : null)
                        .className(s.getSchoolClass() != null ? s.getSchoolClass().getName() : null)
                        .build())
                .collect(Collectors.toList());
    }

    @GetMapping("/hierarchy")
    @Operation(summary = "Get school, branch, and class hierarchy for context selector")
    public List<Map<String, Object>> getHierarchy() {
        List<School> schools = schoolRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (School s : schools) {
            Map<String, Object> sMap = new HashMap<>();
            sMap.put("id", s.getId());
            sMap.put("name", s.getName());
            sMap.put("code", s.getCode());

            List<Branch> branches = branchRepository.findBySchoolId(s.getId());
            List<Map<String, Object>> bList = new ArrayList<>();
            for (Branch b : branches) {
                Map<String, Object> bMap = new HashMap<>();
                bMap.put("id", b.getId());
                bMap.put("name", b.getName());
                bMap.put("code", b.getCode());

                List<SchoolClass> classes = schoolClassRepository.findByBranchId(b.getId());
                List<Map<String, Object>> cList = new ArrayList<>();
                for (SchoolClass c : classes) {
                    Map<String, Object> cMap = new HashMap<>();
                    cMap.put("id", c.getId());
                    cMap.put("name", c.getName());
                    cMap.put("gradeLevel", c.getGradeLevel());
                    cMap.put("section", c.getSection());
                    cList.add(cMap);
                }
                bMap.put("classes", cList);
                bList.add(bMap);
            }
            sMap.put("branches", bList);
            result.add(sMap);
        }

        return result;
    }
}
