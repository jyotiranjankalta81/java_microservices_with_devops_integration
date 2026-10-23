package com.jobintel.evidencing.service;

import com.jobintel.evidencing.domain.entity.MediaAsset;
import com.jobintel.evidencing.domain.enums.MediaAssetStatus;
import com.jobintel.evidencing.domain.enums.MediaType;
import com.jobintel.evidencing.dto.MediaAssetResponse;
import com.jobintel.evidencing.dto.MediaInitiateRequest;
import com.jobintel.evidencing.dto.MediaInitiateResponse;
import com.jobintel.evidencing.exception.EvidencingAccessDeniedException;
import com.jobintel.evidencing.repository.MediaAssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaService {

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );
    private static final Set<String> ALLOWED_VIDEO_TYPES = Set.of(
            "video/mp4", "video/quicktime", "video/webm"
    );
    private static final long MAX_IMAGE_SIZE = 50 * 1024 * 1024L; // 50MB
    private static final long MAX_VIDEO_SIZE = 500 * 1024 * 1024L; // 500MB

    private final MediaAssetRepository mediaAssetRepository;
    private final StorageService storageService;
    private final S3StorageService s3StorageService;
    private final MediaProcessingService mediaProcessingService;
    private final EvidencingAccessService accessService;

    @Transactional
    public MediaInitiateResponse initiateUpload(UUID userId, Collection<String> roles, MediaInitiateRequest request) {
        accessService.checkModuleCreateAccess(userId, roles);

        String contentType = request.getContentType().toLowerCase().trim();
        long size = request.getFileSizeBytes();

        if (request.getMediaType() == MediaType.IMAGE) {
            if (!ALLOWED_IMAGE_TYPES.contains(contentType)) {
                throw new IllegalArgumentException("Unsupported image format: " + contentType);
            }
            if (size > MAX_IMAGE_SIZE) {
                throw new IllegalArgumentException("Image size exceeds limit of 50MB");
            }
        } else if (request.getMediaType() == MediaType.VIDEO) {
            if (!ALLOWED_VIDEO_TYPES.contains(contentType)) {
                throw new IllegalArgumentException("Unsupported video format: " + contentType);
            }
            if (size > MAX_VIDEO_SIZE) {
                throw new IllegalArgumentException("Video size exceeds limit of 500MB");
            }
        }

        UUID mediaId = UUID.randomUUID();
        String sanitizedFilename = request.getOriginalFilename().replaceAll("[^a-zA-Z0-9._-]", "_");
        String storageKey = "media/" + userId + "/" + mediaId + "_" + sanitizedFilename;

        MediaAsset asset = MediaAsset.builder()
                .id(mediaId)
                .uploaderId(userId)
                .mediaType(request.getMediaType())
                .originalFilename(request.getOriginalFilename())
                .contentType(contentType)
                .fileSizeBytes(size)
                .storageKey(storageKey)
                .status(MediaAssetStatus.UPLOADING)
                .build();

        mediaAssetRepository.save(asset);

        String uploadUrl = storageService.generateUploadUrl(storageKey, contentType);

        return MediaInitiateResponse.builder()
                .mediaAssetId(mediaId)
                .uploadUrl(uploadUrl)
                .storageKey(storageKey)
                .mediaType(request.getMediaType())
                .status(MediaAssetStatus.UPLOADING)
                .build();
    }

    @Transactional
    public MediaAssetResponse completeUpload(UUID userId, UUID mediaAssetId) {
        MediaAsset asset = mediaAssetRepository.findById(mediaAssetId)
                .orElseThrow(() -> new IllegalArgumentException("MediaAsset not found: " + mediaAssetId));

        if (!asset.getUploaderId().equals(userId)) {
            throw new EvidencingAccessDeniedException("Access Denied: You cannot manage someone else's media upload.");
        }

        asset.setStatus(MediaAssetStatus.PROCESSING);
        mediaAssetRepository.saveAndFlush(asset);

        // Trigger variant maker asynchronously
        mediaProcessingService.processMediaAsync(mediaAssetId);

        return mapToResponse(asset, userId, List.of());
    }

    @Transactional(readOnly = true)
    public MediaAssetResponse getMediaStatus(UUID userId, Collection<String> roles, UUID mediaAssetId) {
        MediaAsset asset = mediaAssetRepository.findById(mediaAssetId)
                .orElseThrow(() -> new IllegalArgumentException("MediaAsset not found: " + mediaAssetId));

        accessService.checkCanAccessMedia(userId, roles, asset);

        return mapToResponse(asset, userId, roles);
    }

    @Transactional
    public MediaAssetResponse retryMedia(UUID userId, Collection<String> roles, UUID mediaAssetId) {
        accessService.checkModuleCreateAccess(userId, roles);
        MediaAsset asset = mediaProcessingService.retryProcessing(mediaAssetId, userId);
        return mapToResponse(asset, userId, roles);
    }

    public void handleDirectUpload(String storageKey, String token, long expires, byte[] data, String contentType) {
        if (!s3StorageService.verifyHmacToken(storageKey, expires, token)) {
            throw new EvidencingAccessDeniedException("Invalid or expired upload authorization token.");
        }
        storageService.putObject(storageKey, data, contentType);
    }

    public InputStream handleDirectDownload(String storageKey, String token, long expires) {
        if (!s3StorageService.verifyHmacToken(storageKey, expires, token)) {
            throw new EvidencingAccessDeniedException("Invalid or expired media authorization token.");
        }
        return storageService.getObject(storageKey);
    }

    public MediaAssetResponse mapToResponse(MediaAsset asset, UUID userId, Collection<String> roles) {
        String downloadUrl = null;
        String thumbUrl = null;

        if (asset.getStatus() == MediaAssetStatus.READY) {
            downloadUrl = storageService.generateDownloadUrl(asset.getStorageKey());
            if (asset.getThumbnailStorageKey() != null) {
                thumbUrl = storageService.generateDownloadUrl(asset.getThumbnailStorageKey());
            }
        }

        return MediaAssetResponse.builder()
                .mediaAssetId(asset.getId())
                .originalFilename(asset.getOriginalFilename())
                .mediaType(asset.getMediaType())
                .status(asset.getStatus())
                .url(downloadUrl)
                .thumbnailUrl(thumbUrl != null ? thumbUrl : downloadUrl)
                .width(asset.getWidth())
                .height(asset.getHeight())
                .durationSeconds(asset.getDurationSeconds())
                .errorMessage(asset.getErrorMessage())
                .build();
    }
}
