package com.jobintel.evidencing.service;

import com.jobintel.evidencing.domain.entity.EvidencingNotification;
import com.jobintel.evidencing.domain.entity.Post;
import com.jobintel.evidencing.domain.entity.PostRecipient;
import com.jobintel.evidencing.domain.entity.UserScope;
import com.jobintel.evidencing.domain.enums.EvidencingPermission;
import com.jobintel.evidencing.domain.enums.PostVisibility;
import com.jobintel.evidencing.dto.NotificationResponse;
import com.jobintel.evidencing.repository.EvidencingNotificationRepository;
import com.jobintel.evidencing.repository.PostRecipientRepository;
import com.jobintel.evidencing.repository.PostRepository;
import com.jobintel.evidencing.repository.UserScopeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EvidencingNotificationRepository notificationRepository;
    private final PostRepository postRepository;
    private final PostRecipientRepository postRecipientRepository;
    private final UserScopeRepository userScopeRepository;
    private final EvidencingAccessService accessService;

    @Transactional
    public void generateAndSendNotifications(UUID postId) {
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) {
            log.warn("Cannot generate notifications: post {} not found", postId);
            return;
        }

        UserScope creatorScope = userScopeRepository.findByUserId(post.getAuthorId()).orElse(null);
        String creatorName = creatorScope != null ? creatorScope.getDisplayName() : "A staff member";

        Set<UUID> targetUserIds = new HashSet<>();

        if (post.getVisibility() == PostVisibility.PRIVATE) {
            // PRIVATE: notify ONLY tagged recipients who have permission to view it
            List<PostRecipient> recipients = postRecipientRepository.findByPostId(postId);
            for (PostRecipient pr : recipients) {
                UUID recipientId = pr.getUserId();
                // Exclude creator
                if (!recipientId.equals(post.getAuthorId())) {
                    UserScope scope = userScopeRepository.findByUserId(recipientId).orElse(null);
                    if (scope != null && accessService.hasPermission(List.of(scope.getRole()), EvidencingPermission.EVIDENCING_VIEW.name())) {
                        targetUserIds.add(recipientId);
                    }
                }
            }
        } else {
            // PUBLIC: notify eligible users within post's school/branch/class audience
            List<UserScope> branchUsers = userScopeRepository.findByBranchId(post.getBranch().getId());
            for (UserScope scope : branchUsers) {
                UUID userId = scope.getUserId();
                // Exclude creator
                if (userId.equals(post.getAuthorId())) {
                    continue;
                }
                // Must have EVIDENCING_VIEW
                if (!accessService.hasPermission(List.of(scope.getRole()), EvidencingPermission.EVIDENCING_VIEW.name())) {
                    continue;
                }
                // Class filter check: if post is class-specific and recipient is a teacher of a different class, exclude
                if (post.getSchoolClass() != null && "TEACHER".equalsIgnoreCase(scope.getRole()) && scope.getSchoolClass() != null) {
                    if (!scope.getSchoolClass().getId().equals(post.getSchoolClass().getId())) {
                        continue;
                    }
                }
                targetUserIds.add(userId);
            }
        }

        String shortMessage = String.format("%s published: %s",
                creatorName,
                post.getCaption() != null && post.getCaption().length() > 60
                        ? post.getCaption().substring(0, 57) + "..."
                        : (post.getCaption() != null ? post.getCaption() : "New post"));

        String link = "/evidencing/" + postId;

        for (UUID recipientId : targetUserIds) {
            String idempotencyKey = "notif-" + postId + "-" + recipientId;
            if (notificationRepository.findByIdempotencyKey(idempotencyKey).isPresent()) {
                log.debug("Notification with key {} already delivered. Skipping duplicate.", idempotencyKey);
                continue;
            }

            EvidencingNotification notif = EvidencingNotification.builder()
                    .userId(recipientId)
                    .post(post)
                    .creatorId(post.getAuthorId())
                    .creatorName(creatorName)
                    .message(shortMessage)
                    .read(false)
                    .link(link)
                    .idempotencyKey(idempotencyKey)
                    .build();

            notificationRepository.save(notif);
            log.info("In-app & push notification delivered to user {} for post {}", recipientId, postId);
        }
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public void markAsRead(UUID notificationId, UUID userId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            if (n.getUserId().equals(userId)) {
                n.setRead(true);
                notificationRepository.save(n);
            }
        });
    }

    @Transactional
    public void markAllAsRead(UUID userId) {
        List<EvidencingNotification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        for (EvidencingNotification n : notifications) {
            if (!n.isRead()) {
                n.setRead(true);
                notificationRepository.save(n);
            }
        }
    }

    private NotificationResponse mapToResponse(EvidencingNotification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .postId(n.getPost().getId())
                .creatorId(n.getCreatorId())
                .creatorName(n.getCreatorName())
                .message(n.getMessage())
                .read(n.isRead())
                .link(n.getLink())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
