package com.jobintel.evidencing.web;

import com.jobintel.evidencing.dto.MediaAssetResponse;
import com.jobintel.evidencing.dto.MediaInitiateRequest;
import com.jobintel.evidencing.dto.MediaInitiateResponse;
import com.jobintel.evidencing.service.MediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/evidencing/media")
@RequiredArgsConstructor
@Tag(name = "Evidencing Media", description = "Endpoints for authorized media upload, variant processing, and retrieval")
public class MediaController {

    private final MediaService mediaService;
    private final UserContextHelper userContextHelper;

    @PostMapping("/initiate-upload")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Authorize media upload and generate secure upload target URL")
    public MediaInitiateResponse initiateUpload(
            @Valid @RequestBody MediaInitiateRequest request,
            HttpServletRequest httpRequest) {

        UUID userId = userContextHelper.getUserId(httpRequest);
        List<String> roles = userContextHelper.getUserRoles(httpRequest);

        return mediaService.initiateUpload(userId, roles, request);
    }

    @PostMapping("/{id}/complete-upload")
    @Operation(summary = "Notify upload completion and trigger Media Variant Maker asynchronously")
    public MediaAssetResponse completeUpload(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {

        UUID userId = userContextHelper.getUserId(httpRequest);
        return mediaService.completeUpload(userId, id);
    }

    @GetMapping("/{id}/status")
    @Operation(summary = "Get media processing status and authorized short-lived URL")
    public MediaAssetResponse getMediaStatus(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {

        UUID userId = userContextHelper.getUserId(httpRequest);
        List<String> roles = userContextHelper.getUserRoles(httpRequest);

        return mediaService.getMediaStatus(userId, roles, id);
    }

    @PostMapping("/{id}/retry")
    @Operation(summary = "Retry failed media processing")
    public MediaAssetResponse retryMedia(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {

        UUID userId = userContextHelper.getUserId(httpRequest);
        List<String> roles = userContextHelper.getUserRoles(httpRequest);

        return mediaService.retryMedia(userId, roles, id);
    }

    @PutMapping("/upload-direct")
    @Operation(summary = "Direct upload target for local/mock S3 fallback")
    public ResponseEntity<Void> uploadDirect(
            @RequestParam("key") String storageKey,
            @RequestParam("token") String token,
            @RequestParam("expires") long expires,
            @RequestBody byte[] data,
            @RequestHeader(value = HttpHeaders.CONTENT_TYPE, defaultValue = "application/octet-stream") String contentType) {

        mediaService.handleDirectUpload(storageKey, token, expires, data, contentType);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/file")
    @Operation(summary = "Direct download target for local/mock S3 fallback")
    public ResponseEntity<InputStreamResource> downloadDirect(
            @RequestParam("key") String storageKey,
            @RequestParam("token") String token,
            @RequestParam("expires") long expires) {

        InputStream stream = mediaService.handleDirectDownload(storageKey, token, expires);
        if (stream == null) {
            return ResponseEntity.notFound().build();
        }

        String mime = storageKey.endsWith(".mp4") ? "video/mp4" : "image/jpeg";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mime))
                .body(new InputStreamResource(stream));
    }
}
