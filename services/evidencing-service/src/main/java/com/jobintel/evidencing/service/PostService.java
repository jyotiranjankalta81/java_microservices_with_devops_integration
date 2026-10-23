package com.jobintel.evidencing.service;

import com.jobintel.evidencing.domain.entity.*;
import com.jobintel.evidencing.domain.enums.MediaAssetStatus;
import com.jobintel.evidencing.domain.enums.PostVisibility;
import com.jobintel.evidencing.domain.enums.PublicationStatus;
import com.jobintel.evidencing.dto.*;
import com.jobintel.evidencing.exception.EvidencingAccessDeniedException;
import com.jobintel.evidencing.exception.MediaNotReadyException;
import com.jobintel.evidencing.repository.*;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostMediaRepository postMediaRepository;
    private final PostRecipientRepository postRecipientRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostCommentRepository postCommentRepository;
    private final PostViewRepository postViewRepository;
    private final MediaAssetRepository mediaAssetRepository;
    private final SchoolRepository schoolRepository;
    private final BranchRepository branchRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final UserScopeRepository userScopeRepository;
    private final CategoryService categoryService;
    private final EvidencingAccessService accessService;
    private final QueueService queueService;
    private final StorageService storageService;

    @Transactional
    public PostResponse publishPost(UUID userId, Collection<String> roles, PostCreateRequest request, String idempotencyHeader) {
        String idempotencyKey = (idempotencyHeader != null && !idempotencyHeader.isBlank())
                ? idempotencyHeader.trim()
                : (request.getIdempotencyKey() != null ? request.getIdempotencyKey().trim() : null);

        // Idempotency check: if key already exists, return existing post
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<Post> existing = postRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                log.info("Duplicate publication request intercepted for key {}. Returning existing post.", idempotencyKey);
                return getPost(userId, roles, existing.get().getId());
            }
        }

        // Scope & Permission validation (deny-by-default)
        accessService.validatePostCreationScope(userId, roles, request.getSchoolId(), request.getBranchId(), request.getClassId());

        School school = schoolRepository.findById(request.getSchoolId())
                .orElseThrow(() -> new IllegalArgumentException("School not found: " + request.getSchoolId()));
        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new IllegalArgumentException("Branch not found: " + request.getBranchId()));
        SchoolClass schoolClass = request.getClassId() != null
                ? schoolClassRepository.findById(request.getClassId()).orElse(null)
                : null;

        PostCategory category = categoryService.getActiveCategoryById(request.getCategoryId());

        // Validate Public vs Private
        List<UUID> recipientIds = new ArrayList<>();
        if (request.getVisibility() == PostVisibility.PRIVATE) {
            if (request.getTaggedRecipientIds() == null || request.getTaggedRecipientIds().isEmpty()) {
                throw new IllegalArgumentException("Private posts require at least one tagged recipient.");
            }
            recipientIds.addAll(request.getTaggedRecipientIds());
            // Validate recipients are eligible and within school/branch scope
            accessService.validateTaggedRecipients(request.getSchoolId(), request.getBranchId(), recipientIds);
        } else {
            // For PUBLIC posts: store NO audience tags
            recipientIds.clear();
        }

        // Validate Media Assets
        List<MediaAsset> mediaAssets = mediaAssetRepository.findByIdIn(request.getMediaAssetIds());
        if (mediaAssets.size() != request.getMediaAssetIds().size()) {
            throw new IllegalArgumentException("One or more media assets were not found.");
        }

        // Validate ownership & readiness
        for (MediaAsset m : mediaAssets) {
            if (!m.getUploaderId().equals(userId)) {
                throw new EvidencingAccessDeniedException("Access Denied: You cannot attach media uploaded by another user.");
            }
            if (m.getStatus() != MediaAssetStatus.READY) {
                throw new MediaNotReadyException("Cannot publish: Media '" + m.getOriginalFilename() +
                        "' is in status " + m.getStatus() + ". Publication requires all media to be READY.");
            }
        }

        // Build Post Entity in PUBLISHING status
        Post post = Post.builder()
                .authorId(userId)
                .school(school)
                .branch(branch)
                .schoolClass(schoolClass)
                .category(category)
                .caption(request.getCaption())
                .visibility(request.getVisibility())
                .publicationStatus(PublicationStatus.PUBLISHING)
                .idempotencyKey(idempotencyKey)
                .publishedAt(null)
                .build();

        Post savedPost = postRepository.save(post);

        // Attach media in display order
        int order = 0;
        for (UUID mediaId : request.getMediaAssetIds()) {
            MediaAsset ma = mediaAssets.stream().filter(a -> a.getId().equals(mediaId)).findFirst().orElse(null);
            if (ma != null) {
                PostMedia pm = PostMedia.builder()
                        .post(savedPost)
                        .mediaAsset(ma)
                        .displayOrder(order++)
                        .build();
                postMediaRepository.save(pm);
                savedPost.getPostMediaList().add(pm);
            }
        }

        // Attach recipients for private posts
        if (request.getVisibility() == PostVisibility.PRIVATE) {
            for (UUID recId : recipientIds) {
                PostRecipient pr = PostRecipient.builder()
                        .post(savedPost)
                        .userId(recId)
                        .build();
                postRecipientRepository.save(pr);
                savedPost.getRecipients().add(pr);
            }
        }

        // Queue publication asynchronously via SQS
        queueService.enqueuePostPublication(savedPost.getId(), idempotencyKey);

        return enrichPostResponse(savedPost, userId, roles);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getFeed(UUID userId, Collection<String> roles, PostFilterCriteria criteria, Pageable pageable) {
        // Module permission check
        accessService.checkModuleViewAccess(userId, roles);

        // Preload user scope and tagged post IDs once for instant O(1) in-memory checks
        UserScope userScope = userScopeRepository.findByUserId(userId).orElse(null);
        Set<UUID> taggedPostIds = new HashSet<>(postRecipientRepository.findPostIdsByUserId(userId));

        // Retrieve all candidate posts in user's authorized scope
        List<Post> allPosts = postRepository.findAll();

        // 1. Centralized Authorization & Visibility Filtering
        List<Post> visiblePosts = allPosts.stream()
                .filter(p -> p.getPublicationStatus() == PublicationStatus.PUBLISHED || p.getAuthorId().equals(userId))
                .filter(p -> accessService.canViewPost(userId, roles, p, userScope, taggedPostIds))
                .collect(Collectors.toList());

        // 2. Additional criteria filtering
        if (criteria != null) {
            if (criteria.getCategoryId() != null) {
                visiblePosts = visiblePosts.stream()
                        .filter(p -> p.getCategory().getId().equals(criteria.getCategoryId()))
                        .collect(Collectors.toList());
            }
            if (criteria.getAuthorId() != null) {
                visiblePosts = visiblePosts.stream()
                        .filter(p -> p.getAuthorId().equals(criteria.getAuthorId()))
                        .collect(Collectors.toList());
            }
            if (criteria.getVisibility() != null) {
                visiblePosts = visiblePosts.stream()
                        .filter(p -> p.getVisibility() == criteria.getVisibility())
                        .collect(Collectors.toList());
            }
            if (criteria.getClassId() != null) {
                visiblePosts = visiblePosts.stream()
                        .filter(p -> p.getSchoolClass() != null && p.getSchoolClass().getId().equals(criteria.getClassId()))
                        .collect(Collectors.toList());
            }
            if (criteria.getBranchId() != null) {
                visiblePosts = visiblePosts.stream()
                        .filter(p -> p.getBranch().getId().equals(criteria.getBranchId()))
                        .collect(Collectors.toList());
            }
            if (criteria.getSearch() != null && !criteria.getSearch().isBlank()) {
                String q = criteria.getSearch().toLowerCase().trim();
                visiblePosts = visiblePosts.stream()
                        .filter(p -> (p.getCaption() != null && p.getCaption().toLowerCase().contains(q)) ||
                                (p.getCategory().getName() != null && p.getCategory().getName().toLowerCase().contains(q)))
                        .collect(Collectors.toList());
            }
            if (criteria.getFromDate() != null) {
                visiblePosts = visiblePosts.stream()
                        .filter(p -> p.getCreatedAt().isAfter(criteria.getFromDate()) || p.getCreatedAt().equals(criteria.getFromDate()))
                        .collect(Collectors.toList());
            }
            if (criteria.getToDate() != null) {
                visiblePosts = visiblePosts.stream()
                        .filter(p -> p.getCreatedAt().isBefore(criteria.getToDate()) || p.getCreatedAt().equals(criteria.getToDate()))
                        .collect(Collectors.toList());
            }
        }

        // Sort by publication time / created time descending
        visiblePosts.sort((a, b) -> {
            Instant tA = a.getPublishedAt() != null ? a.getPublishedAt() : a.getCreatedAt();
            Instant tB = b.getPublishedAt() != null ? b.getPublishedAt() : b.getCreatedAt();
            return tB.compareTo(tA);
        });

        // 3. Paginate
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), visiblePosts.size());
        List<Post> paged = start <= end ? visiblePosts.subList(start, end) : Collections.emptyList();

        // 4. Batch Enrich display-ready details
        List<PostResponse> responses = paged.stream()
                .map(p -> enrichPostResponse(p, userId, roles))
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, visiblePosts.size());
    }

    @Transactional
    public PostResponse getPost(UUID userId, Collection<String> roles, UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with ID: " + postId));

        // Centralized View Check
        accessService.checkCanViewPost(userId, roles, post);

        // Record post view if not already viewed by this user
        if (!postViewRepository.existsByPostIdAndUserId(postId, userId)) {
            PostView view = PostView.builder()
                    .post(post)
                    .userId(userId)
                    .build();
            postViewRepository.save(view);
        }

        return enrichPostResponse(post, userId, roles);
    }

    @Transactional
    public PostResponse updatePost(UUID userId, Collection<String> roles, UUID postId, PostUpdateRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with ID: " + postId));

        // Centralized Edit Check
        accessService.checkCanEditPost(userId, roles, post);

        // Optimistic concurrency check
        if (!post.getVersion().equals(request.getVersion())) {
            throw new OptimisticLockException("The post was updated by another user or session. Please refresh and try again.");
        }

        PostCategory category = categoryService.getActiveCategoryById(request.getCategoryId());
        post.setCategory(category);
        post.setCaption(request.getCaption());

        // Visibility transition handling
        PostVisibility oldVisibility = post.getVisibility();
        PostVisibility newVisibility = request.getVisibility();

        if (oldVisibility == PostVisibility.PRIVATE && newVisibility == PostVisibility.PUBLIC) {
            // Audience expands: clear tags
            postRecipientRepository.deleteByPostId(postId);
            post.getRecipients().clear();
            post.setVisibility(PostVisibility.PUBLIC);
        } else if (oldVisibility == PostVisibility.PUBLIC && newVisibility == PostVisibility.PRIVATE) {
            // Audience restricts: require recipients
            if (request.getTaggedRecipientIds() == null || request.getTaggedRecipientIds().isEmpty()) {
                throw new IllegalArgumentException("Switching to Private requires at least one tagged recipient.");
            }
            accessService.validateTaggedRecipients(post.getSchool().getId(), post.getBranch().getId(), request.getTaggedRecipientIds());
            post.setVisibility(PostVisibility.PRIVATE);
            for (UUID recId : request.getTaggedRecipientIds()) {
                PostRecipient pr = PostRecipient.builder()
                        .post(post)
                        .userId(recId)
                        .build();
                postRecipientRepository.save(pr);
                post.getRecipients().add(pr);
            }
        } else if (newVisibility == PostVisibility.PRIVATE) {
            // Updating existing private post recipients
            if (request.getTaggedRecipientIds() == null || request.getTaggedRecipientIds().isEmpty()) {
                throw new IllegalArgumentException("Private posts require at least one tagged recipient.");
            }
            accessService.validateTaggedRecipients(post.getSchool().getId(), post.getBranch().getId(), request.getTaggedRecipientIds());
            postRecipientRepository.deleteByPostId(postId);
            post.getRecipients().clear();
            for (UUID recId : request.getTaggedRecipientIds()) {
                PostRecipient pr = PostRecipient.builder()
                        .post(post)
                        .userId(recId)
                        .build();
                postRecipientRepository.save(pr);
                post.getRecipients().add(pr);
            }
        }

        // Update media if provided
        if (request.getMediaAssetIds() != null && !request.getMediaAssetIds().isEmpty()) {
            postMediaRepository.deleteByPostId(postId);
            post.getPostMediaList().clear();
            int order = 0;
            for (UUID mediaId : request.getMediaAssetIds()) {
                MediaAsset ma = mediaAssetRepository.findById(mediaId).orElse(null);
                if (ma != null && ma.getUploaderId().equals(userId)) {
                    PostMedia pm = PostMedia.builder()
                            .post(post)
                            .mediaAsset(ma)
                            .displayOrder(order++)
                            .build();
                    postMediaRepository.save(pm);
                    post.getPostMediaList().add(pm);
                }
            }
        }

        Post saved = postRepository.save(post);
        return enrichPostResponse(saved, userId, roles);
    }

    @Transactional
    public void deletePost(UUID userId, Collection<String> roles, UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with ID: " + postId));

        accessService.checkCanDeletePost(userId, roles, post);

        postRepository.delete(post);
        log.info("Post {} deleted by user {}", postId, userId);
    }

    /**
     * Enriches a Post entity into a display-ready PostResponse with:
     * - Creator profile (name, avatar, role)
     * - Tagged people (empty for public posts; only returned to authorized viewers for private posts)
     * - Media items with authorized presigned URLs
     * - Interaction counts and current-user flags
     * - Current-user permissions (canEdit, canDelete, canLike, canComment)
     */
    public PostResponse enrichPostResponse(Post post, UUID currentUserId, Collection<String> roles) {
        // Creator details
        UserScope creatorScope = userScopeRepository.findByUserId(post.getAuthorId()).orElse(null);
        String creatorName = creatorScope != null ? creatorScope.getDisplayName() : "Staff Member";
        String creatorAvatar = creatorScope != null ? creatorScope.getAvatarUrl() : null;
        String creatorRole = creatorScope != null ? creatorScope.getRole() : "Staff";

        // Tagged people (empty for public posts; only shown if user is authorized to see private post)
        List<RecipientDetail> taggedPeople = new ArrayList<>();
        if (post.getVisibility() == PostVisibility.PRIVATE) {
            List<PostRecipient> recipients = postRecipientRepository.findByPostId(post.getId());
            List<UUID> recipientUserIds = recipients.stream().map(PostRecipient::getUserId).collect(Collectors.toList());
            List<UserScope> recipientScopes = userScopeRepository.findByUserIdIn(recipientUserIds);
            for (UserScope rs : recipientScopes) {
                taggedPeople.add(RecipientDetail.builder()
                        .userId(rs.getUserId())
                        .displayName(rs.getDisplayName())
                        .avatarUrl(rs.getAvatarUrl())
                        .role(rs.getRole())
                        .build());
            }
        }

        // Media details (ordered, with authorized short-lived signed URLs)
        List<PostMedia> postMediaList = postMediaRepository.findByPostIdOrderByDisplayOrderAsc(post.getId());
        List<MediaDetail> mediaDetails = new ArrayList<>();
        for (PostMedia pm : postMediaList) {
            MediaAsset ma = pm.getMediaAsset();
            String downloadUrl = storageService.generateDownloadUrl(ma.getStorageKey());
            String thumbUrl = ma.getThumbnailStorageKey() != null
                    ? storageService.generateDownloadUrl(ma.getThumbnailStorageKey())
                    : downloadUrl;

            mediaDetails.add(MediaDetail.builder()
                    .mediaId(ma.getId())
                    .mediaType(ma.getMediaType())
                    .url(downloadUrl)
                    .thumbnailUrl(thumbUrl)
                    .width(ma.getWidth())
                    .height(ma.getHeight())
                    .durationSeconds(ma.getDurationSeconds())
                    .displayOrder(pm.getDisplayOrder())
                    .build());
        }

        // Interaction details
        long likeCount = postLikeRepository.countByPostId(post.getId());
        long commentCount = postCommentRepository.countByPostId(post.getId());
        long viewCount = postViewRepository.countByPostId(post.getId());
        boolean isLiked = postLikeRepository.existsByPostIdAndUserId(post.getId(), currentUserId);

        // Permissions
        boolean canEdit = accessService.canEditPost(currentUserId, roles, post);
        boolean canDelete = accessService.canDeletePost(currentUserId, roles, post);
        boolean canLike = accessService.canViewPost(currentUserId, roles, post);
        boolean canComment = canLike;

        return PostResponse.builder()
                .id(post.getId())
                .caption(post.getCaption())
                .visibility(post.getVisibility())
                .publicationStatus(post.getPublicationStatus())
                .publishedAt(post.getPublishedAt())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .version(post.getVersion())
                .schoolId(post.getSchool().getId())
                .schoolName(post.getSchool().getName())
                .branchId(post.getBranch().getId())
                .branchName(post.getBranch().getName())
                .classId(post.getSchoolClass() != null ? post.getSchoolClass().getId() : null)
                .className(post.getSchoolClass() != null ? post.getSchoolClass().getName() : null)
                .authorId(post.getAuthorId())
                .authorName(creatorName)
                .authorAvatarUrl(creatorAvatar)
                .authorRole(creatorRole)
                .taggedPeople(taggedPeople)
                .domainId(post.getCategory().getId())
                .domainName(post.getCategory().getName())
                .domainDescription(post.getCategory().getDescription())
                .domainIcon(post.getCategory().getIcon())
                .media(mediaDetails)
                .likeCount(likeCount)
                .commentCount(commentCount)
                .viewCount(viewCount)
                .isLikedByCurrentUser(isLiked)
                .canEdit(canEdit)
                .canDelete(canDelete)
                .canLike(canLike)
                .canComment(canComment)
                .build();
    }
}
