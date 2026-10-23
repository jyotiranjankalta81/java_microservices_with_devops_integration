package com.jobintel.evidencing.dto;

import com.jobintel.evidencing.domain.enums.PostVisibility;
import com.jobintel.evidencing.domain.enums.PublicationStatus;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponse {

    // Post details
    private UUID id;
    private String caption;
    private PostVisibility visibility;
    private PublicationStatus publicationStatus;
    private Instant publishedAt;
    private Instant createdAt;
    private Instant updatedAt;
    private Long version;

    // School/Branch/Class context
    private UUID schoolId;
    private String schoolName;
    private UUID branchId;
    private String branchName;
    private UUID classId;
    private String className;

    // Creator details
    private UUID authorId;
    private String authorName;
    private String authorAvatarUrl;
    private String authorRole;

    // Tagged people details (empty for public posts; only shown to authorized viewers for private posts)
    @Builder.Default
    private List<RecipientDetail> taggedPeople = new ArrayList<>();

    // Domain details
    private UUID domainId;
    private String domainName;
    private String domainDescription;
    private String domainIcon;

    // Media details (ordered)
    @Builder.Default
    private List<MediaDetail> media = new ArrayList<>();

    // Interaction details
    private long likeCount;
    private long commentCount;
    private long viewCount;
    private boolean isLikedByCurrentUser;

    // Current-user permissions
    private boolean canEdit;
    private boolean canDelete;
    private boolean canLike;
    private boolean canComment;
}
