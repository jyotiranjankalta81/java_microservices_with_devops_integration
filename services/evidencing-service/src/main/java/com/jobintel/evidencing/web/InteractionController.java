package com.jobintel.evidencing.web;

import com.jobintel.evidencing.dto.CommentCreateRequest;
import com.jobintel.evidencing.dto.CommentResponse;
import com.jobintel.evidencing.service.InteractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/evidencing/posts/{id}")
@RequiredArgsConstructor
@Tag(name = "Evidencing Interactions", description = "Endpoints for likes, comments, and interactions")
public class InteractionController {

    private final InteractionService interactionService;
    private final UserContextHelper userContextHelper;

    @PostMapping("/like")
    @Operation(summary = "Toggle like/unlike on a post")
    public Map<String, Object> toggleLike(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {

        UUID userId = userContextHelper.getUserId(httpRequest);
        List<String> roles = userContextHelper.getUserRoles(httpRequest);

        return interactionService.toggleLike(userId, roles, id);
    }

    @PostMapping("/comments")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a comment to a post")
    public CommentResponse addComment(
            @PathVariable UUID id,
            @Valid @RequestBody CommentCreateRequest request,
            HttpServletRequest httpRequest) {

        UUID userId = userContextHelper.getUserId(httpRequest);
        List<String> roles = userContextHelper.getUserRoles(httpRequest);

        return interactionService.addComment(userId, roles, id, request);
    }

    @GetMapping("/comments")
    @Operation(summary = "Get comments for a post")
    public List<CommentResponse> getComments(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {

        UUID userId = userContextHelper.getUserId(httpRequest);
        List<String> roles = userContextHelper.getUserRoles(httpRequest);

        return interactionService.getComments(userId, roles, id);
    }
}
