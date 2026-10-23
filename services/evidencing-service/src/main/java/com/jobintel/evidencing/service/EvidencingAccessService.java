package com.jobintel.evidencing.service;

import com.jobintel.evidencing.domain.entity.MediaAsset;
import com.jobintel.evidencing.domain.entity.Post;
import com.jobintel.evidencing.domain.entity.PostMedia;
import com.jobintel.evidencing.domain.entity.UserScope;
import com.jobintel.evidencing.domain.enums.EvidencingPermission;
import com.jobintel.evidencing.domain.enums.PostVisibility;
import com.jobintel.evidencing.exception.EvidencingAccessDeniedException;
import com.jobintel.evidencing.exception.ScopeValidationException;
import com.jobintel.evidencing.repository.PostMediaRepository;
import com.jobintel.evidencing.repository.PostRecipientRepository;
import com.jobintel.evidencing.repository.RolePermissionRepository;
import com.jobintel.evidencing.repository.UserScopeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EvidencingAccessService {

    private final RolePermissionRepository rolePermissionRepository;
    private final UserScopeRepository userScopeRepository;
    private final PostRecipientRepository postRecipientRepository;
    private final PostMediaRepository postMediaRepository;

    public void checkModuleViewAccess(UUID userId, Collection<String> roles) {
        if (!hasPermission(roles, EvidencingPermission.EVIDENCING_VIEW.name())) {
            throw new EvidencingAccessDeniedException("Access Denied: You do not have permission to view the Evidencing module.");
        }
    }

    public void checkModuleCreateAccess(UUID userId, Collection<String> roles) {
        if (!hasPermission(roles, EvidencingPermission.EVIDENCING_CREATE.name())) {
            throw new EvidencingAccessDeniedException("Access Denied: You do not have permission to create Evidencing posts.");
        }
    }

    public boolean hasPermission(Collection<String> roles, String permission) {
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        return rolePermissionRepository.existsByRoleInAndPermission(roles, permission);
    }

    public List<String> getUserPermissions(Collection<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }
        return rolePermissionRepository.findByRoleIn(roles).stream()
                .map(rp -> rp.getPermission())
                .distinct()
                .collect(Collectors.toList());
    }

    public UserScope getUserScope(UUID userId) {
        return userScopeRepository.findByUserId(userId)
                .orElseThrow(() -> new EvidencingAccessDeniedException("Access Denied: No staff scope profile found for user " + userId));
    }

    public Optional<UserScope> findUserScope(UUID userId) {
        return userScopeRepository.findByUserId(userId);
    }

    /**
     * Validates whether a user can create a post within the specified school/branch/class context.
     * Scope validation and module permissions apply deny-by-default.
     */
    public void validatePostCreationScope(UUID userId, Collection<String> roles, UUID schoolId, UUID branchId, UUID classId) {
        checkModuleCreateAccess(userId, roles);

        UserScope scope = getUserScope(userId);

        // School scope validation
        if (!scope.getSchool().getId().equals(schoolId)) {
            throw new ScopeValidationException("Scope mismatch: You cannot create posts for an unrelated school.");
        }

        // Branch scope validation
        if ("SCHOOL_ADMIN".equalsIgnoreCase(scope.getRole()) || 
            "SCHOOL_USER".equalsIgnoreCase(scope.getRole()) ||
            "ADMIN".equalsIgnoreCase(scope.getRole())) {
            // School-level users operate within authorized school and branches
        } else {
            // Branch-level users and teachers operate within assigned branch
            if (!scope.getBranch().getId().equals(branchId)) {
                throw new ScopeValidationException("Scope mismatch: You cannot create posts outside your assigned branch.");
            }
        }

        // Class scope validation (if specified for teacher)
        if (classId != null && scope.getSchoolClass() != null) {
            if (!scope.getSchoolClass().getId().equals(classId)) {
                throw new ScopeValidationException("Scope mismatch: Teachers can only post within their assigned class or branch.");
            }
        }
    }

    /**
     * Checks if a user is authorized to view a specific post.
     * Enforces:
     * 1. Module view permission
     * 2. School and branch scope
     * 3. Class scope
     * 4. Public vs Private visibility (private posts only visible to author and explicitly tagged recipients;
     *    branch/school admins CANNOT bypass untagged private posts)
     */
    public boolean canViewPost(UUID userId, Collection<String> roles, Post post) {
        return canViewPost(userId, roles, post, null, null);
    }

    public boolean canViewPost(UUID userId, Collection<String> roles, Post post, UserScope preloadedScope, Set<UUID> preloadedTaggedPostIds) {
        // 1. Module permission check (deny-by-default)
        if (!hasPermission(roles, EvidencingPermission.EVIDENCING_VIEW.name())) {
            return false;
        }

        UserScope scope = preloadedScope;
        if (scope == null) {
            Optional<UserScope> scopeOpt = userScopeRepository.findByUserId(userId);
            if (scopeOpt.isEmpty()) {
                // General platform users or admins without explicit school scope can view public school activity posts
                if (roles.contains("ADMIN") || roles.contains("SCHOOL_ADMIN") || roles.contains("USER")) {
                    return post.getVisibility() == PostVisibility.PUBLIC;
                }
                return false;
            } else {
                scope = scopeOpt.get();
            }
        }

        // 2. School scope validation
        if (!scope.getSchool().getId().equals(post.getSchool().getId())) {
            return false;
        }

        // 3. Branch scope validation
        if (!"SCHOOL_ADMIN".equalsIgnoreCase(scope.getRole()) && 
            !"SCHOOL_USER".equalsIgnoreCase(scope.getRole()) &&
            !"ADMIN".equalsIgnoreCase(scope.getRole())) {
            if (!scope.getBranch().getId().equals(post.getBranch().getId())) {
                return false;
            }
        }

        // 4. Class scope validation (if teacher has assigned class and post has class)
        if (scope.getSchoolClass() != null && post.getSchoolClass() != null) {
            // If post belongs to another class and user is a teacher restricted to their class
            if ("TEACHER".equalsIgnoreCase(scope.getRole()) &&
                    !scope.getSchoolClass().getId().equals(post.getSchoolClass().getId()) &&
                    !post.getAuthorId().equals(userId)) {
                // Check if user is explicitly tagged in private post or if public post is restricted to that class
                if (post.getVisibility() == PostVisibility.PRIVATE) {
                    boolean isTagged = preloadedTaggedPostIds != null
                            ? preloadedTaggedPostIds.contains(post.getId())
                            : postRecipientRepository.existsByPostIdAndUserId(post.getId(), userId);
                    if (!isTagged) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }

        // 5. Visibility check (Private vs Public)
        if (post.getVisibility() == PostVisibility.PRIVATE) {
            // Author retains access
            if (post.getAuthorId().equals(userId)) {
                return true;
            }
            // Tagged recipients
            boolean isTagged = preloadedTaggedPostIds != null
                    ? preloadedTaggedPostIds.contains(post.getId())
                    : postRecipientRepository.existsByPostIdAndUserId(post.getId(), userId);
            // Branch admins and school admins must NOT automatically view untagged private posts!
            return isTagged;
        }

        // PUBLIC post: visible to all users who belong to permitted school/branch audience
        return true;
    }

    public void checkCanViewPost(UUID userId, Collection<String> roles, Post post) {
        if (!canViewPost(userId, roles, post)) {
            throw new EvidencingAccessDeniedException("Access Denied: You are not authorized to view this post.");
        }
    }

    public boolean canEditPost(UUID userId, Collection<String> roles, Post post) {
        // Author check with EDIT_OWN permission
        if (post.getAuthorId().equals(userId)) {
            return hasPermission(roles, EvidencingPermission.EVIDENCING_EDIT_OWN.name());
        }
        // Explicit moderation permission that does not bypass private post visibility
        if (hasPermission(roles, EvidencingPermission.EVIDENCING_MODERATE.name())) {
            return canViewPost(userId, roles, post);
        }
        return false;
    }

    public void checkCanEditPost(UUID userId, Collection<String> roles, Post post) {
        if (!canEditPost(userId, roles, post)) {
            throw new EvidencingAccessDeniedException("Access Denied: You do not have permission to edit this post.");
        }
    }

    public boolean canDeletePost(UUID userId, Collection<String> roles, Post post) {
        // Author check with DELETE_OWN permission
        if (post.getAuthorId().equals(userId)) {
            return hasPermission(roles, EvidencingPermission.EVIDENCING_DELETE_OWN.name());
        }
        if (hasPermission(roles, EvidencingPermission.EVIDENCING_MODERATE.name())) {
            return canViewPost(userId, roles, post);
        }
        return false;
    }

    public void checkCanDeletePost(UUID userId, Collection<String> roles, Post post) {
        if (!canDeletePost(userId, roles, post)) {
            throw new EvidencingAccessDeniedException("Access Denied: You do not have permission to delete this post.");
        }
    }

    /**
     * Authorize media access against post's current visibility and scope before issuing short-lived signed URLs.
     */
    public void checkCanAccessMedia(UUID userId, Collection<String> roles, MediaAsset mediaAsset) {
        // 1. Module permission
        checkModuleViewAccess(userId, roles);

        // 2. If uploader owns the media, allow
        if (mediaAsset.getUploaderId().equals(userId)) {
            return;
        }

        // 3. Find posts where this media is attached
        List<PostMedia> postMediaList = postMediaRepository.findAll().stream()
                .filter(pm -> pm.getMediaAsset().getId().equals(mediaAsset.getId()))
                .collect(Collectors.toList());

        if (postMediaList.isEmpty()) {
            // Unattached media can only be accessed by the uploader
            throw new EvidencingAccessDeniedException("Access Denied: Unattached media can only be accessed by uploader.");
        }

        // Check if user has access to at least one post containing this media
        boolean hasAccess = postMediaList.stream()
                .anyMatch(pm -> canViewPost(userId, roles, pm.getPost()));

        if (!hasAccess) {
            throw new EvidencingAccessDeniedException("Access Denied: You do not have permission to access this post's media.");
        }
    }

    /**
     * Validates that tagged recipients are eligible:
     * - Belong to permitted school/branch scope
     * - Have EVIDENCING_VIEW permission
     */
    public void validateTaggedRecipients(UUID schoolId, UUID branchId, List<UUID> recipientIds) {
        if (recipientIds == null || recipientIds.isEmpty()) {
            return;
        }

        List<UserScope> scopes = userScopeRepository.findByUserIdIn(recipientIds);
        if (scopes.size() != recipientIds.size()) {
            throw new ScopeValidationException("One or more tagged recipients do not exist or have no staff profile.");
        }

        for (UserScope s : scopes) {
            if (!s.getSchool().getId().equals(schoolId) || !s.getBranch().getId().equals(branchId)) {
                throw new ScopeValidationException("Tagged recipient " + s.getDisplayName() + " is outside the post's school/branch scope.");
            }
            if (!hasPermission(List.of(s.getRole()), EvidencingPermission.EVIDENCING_VIEW.name())) {
                throw new ScopeValidationException("Tagged recipient " + s.getDisplayName() + " has no Evidencing view permission.");
            }
        }
    }
}
