package com.jobintel.evidencing.service;

import com.jobintel.evidencing.domain.entity.Post;
import com.jobintel.evidencing.domain.entity.PostComment;
import com.jobintel.evidencing.domain.entity.PostLike;
import com.jobintel.evidencing.domain.entity.UserScope;
import com.jobintel.evidencing.dto.CommentCreateRequest;
import com.jobintel.evidencing.dto.CommentResponse;
import com.jobintel.evidencing.repository.PostCommentRepository;
import com.jobintel.evidencing.repository.PostLikeRepository;
import com.jobintel.evidencing.repository.PostRepository;
import com.jobintel.evidencing.repository.UserScopeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InteractionService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostCommentRepository postCommentRepository;
    private final UserScopeRepository userScopeRepository;
    private final EvidencingAccessService accessService;

    @Transactional
    public Map<String, Object> toggleLike(UUID userId, Collection<String> roles, UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with ID: " + postId));

        // Recheck view authorization
        accessService.checkCanViewPost(userId, roles, post);

        Optional<PostLike> existingLike = postLikeRepository.findByPostIdAndUserId(postId, userId);
        boolean liked;
        if (existingLike.isPresent()) {
            postLikeRepository.delete(existingLike.get());
            liked = false;
        } else {
            PostLike newLike = PostLike.builder()
                    .post(post)
                    .userId(userId)
                    .build();
            postLikeRepository.save(newLike);
            liked = true;
        }

        long count = postLikeRepository.countByPostId(postId);
        Map<String, Object> result = new HashMap<>();
        result.put("postId", postId);
        result.put("liked", liked);
        result.put("likeCount", count);
        return result;
    }

    @Transactional
    public CommentResponse addComment(UUID userId, Collection<String> roles, UUID postId, CommentCreateRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with ID: " + postId));

        // Recheck view authorization
        accessService.checkCanViewPost(userId, roles, post);

        PostComment comment = PostComment.builder()
                .post(post)
                .userId(userId)
                .commentText(request.getCommentText().trim())
                .build();

        PostComment saved = postCommentRepository.save(comment);

        UserScope scope = userScopeRepository.findByUserId(userId).orElse(null);
        String authorName = scope != null ? scope.getDisplayName() : "Staff Member";
        String avatar = scope != null ? scope.getAvatarUrl() : null;
        String role = scope != null ? scope.getRole() : "Staff";

        return CommentResponse.builder()
                .id(saved.getId())
                .postId(postId)
                .userId(userId)
                .authorName(authorName)
                .authorAvatarUrl(avatar)
                .authorRole(role)
                .commentText(saved.getCommentText())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(UUID userId, Collection<String> roles, UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with ID: " + postId));

        // Recheck view authorization
        accessService.checkCanViewPost(userId, roles, post);

        List<PostComment> comments = postCommentRepository.findByPostIdOrderByCreatedAtAsc(postId);
        Set<UUID> userIds = comments.stream().map(PostComment::getUserId).collect(Collectors.toSet());
        Map<UUID, UserScope> scopeMap = userScopeRepository.findByUserIdIn(userIds).stream()
                .collect(Collectors.toMap(UserScope::getUserId, s -> s));

        return comments.stream().map(c -> {
            UserScope scope = scopeMap.get(c.getUserId());
            return CommentResponse.builder()
                    .id(c.getId())
                    .postId(postId)
                    .userId(c.getUserId())
                    .authorName(scope != null ? scope.getDisplayName() : "Staff Member")
                    .authorAvatarUrl(scope != null ? scope.getAvatarUrl() : null)
                    .authorRole(scope != null ? scope.getRole() : "Staff")
                    .commentText(c.getCommentText())
                    .createdAt(c.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());
    }
}
