package com.jobintel.evidencing.web;

import com.jobintel.evidencing.dto.PostCreateRequest;
import com.jobintel.evidencing.dto.PostFilterCriteria;
import com.jobintel.evidencing.dto.PostResponse;
import com.jobintel.evidencing.dto.PostUpdateRequest;
import com.jobintel.evidencing.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/evidencing/posts")
@RequiredArgsConstructor
@Tag(name = "Evidencing Posts", description = "Endpoints for creating, retrieving, editing, and publishing evidencing posts")
public class PostController {

    private final PostService postService;
    private final UserContextHelper userContextHelper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Publish a new evidencing post (with idempotency and scope validation)")
    public PostResponse publishPost(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody PostCreateRequest request,
            HttpServletRequest httpRequest) {

        UUID userId = userContextHelper.getUserId(httpRequest);
        List<String> roles = userContextHelper.getUserRoles(httpRequest);

        return postService.publishPost(userId, roles, request, idempotencyKey);
    }

    @GetMapping
    @Operation(summary = "Retrieve Instagram-style feed with filtering, pagination, and authorization")
    public Page<PostResponse> getFeed(
            PostFilterCriteria criteria,
            @PageableDefault(size = 10) Pageable pageable,
            HttpServletRequest httpRequest) {

        UUID userId = userContextHelper.getUserId(httpRequest);
        List<String> roles = userContextHelper.getUserRoles(httpRequest);

        return postService.getFeed(userId, roles, criteria, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get full post details including creator, category, media signed URLs, and interaction counts")
    public PostResponse getPost(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {

        UUID userId = userContextHelper.getUserId(httpRequest);
        List<String> roles = userContextHelper.getUserRoles(httpRequest);

        return postService.getPost(userId, roles, id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update post (optimistic concurrency via version)")
    public PostResponse updatePost(
            @PathVariable UUID id,
            @Valid @RequestBody PostUpdateRequest request,
            HttpServletRequest httpRequest) {

        UUID userId = userContextHelper.getUserId(httpRequest);
        List<String> roles = userContextHelper.getUserRoles(httpRequest);

        return postService.updatePost(userId, roles, id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete post (author or authorized moderator only)")
    public void deletePost(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {

        UUID userId = userContextHelper.getUserId(httpRequest);
        List<String> roles = userContextHelper.getUserRoles(httpRequest);

        postService.deletePost(userId, roles, id);
    }
}
