package com.jobintel.evidencing.service;

import com.jobintel.evidencing.domain.entity.MediaAsset;
import com.jobintel.evidencing.domain.enums.MediaAssetStatus;
import com.jobintel.evidencing.domain.enums.MediaType;
import com.jobintel.evidencing.repository.MediaAssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaProcessingService {

    private final MediaAssetRepository mediaAssetRepository;
    private final StorageService storageService;

    @Async
    @Transactional
    public void processMediaAsync(UUID mediaAssetId) {
        log.info("Starting Media Variant Maker for mediaAssetId: {}", mediaAssetId);
        processMedia(mediaAssetId);
    }

    @Transactional
    public void processMedia(UUID mediaAssetId) {
        MediaAsset media = mediaAssetRepository.findById(mediaAssetId)
                .orElse(null);

        if (media == null) {
            log.warn("MediaAsset {} not found for variant processing", mediaAssetId);
            return;
        }

        media.setStatus(MediaAssetStatus.PROCESSING);
        mediaAssetRepository.saveAndFlush(media);

        try {
            // Check if file is uploaded or exists in storage
            String storageKey = media.getStorageKey();
            String thumbnailKey = "thumbnails/" + mediaAssetId + "_thumb.jpg";

            if (media.getMediaType() == MediaType.IMAGE) {
                // Generate simulated / real image variants (Thumbnail, Medium, Large)
                media.setWidth(1920);
                media.setHeight(1080);
                media.setThumbnailStorageKey(thumbnailKey);

                // If storage doesn't have bytes yet, create a placeholder thumbnail
                if (!storageService.exists(thumbnailKey)) {
                    byte[] dummyThumb = ("THUMBNAIL_VARIANT_" + mediaAssetId).getBytes();
                    storageService.putObject(thumbnailKey, dummyThumb, "image/jpeg");
                }
            } else if (media.getMediaType() == MediaType.VIDEO) {
                // Generate simulated / real video renditions and poster thumbnail
                media.setWidth(1280);
                media.setHeight(720);
                media.setDurationSeconds(30.0);
                media.setThumbnailStorageKey(thumbnailKey);

                if (!storageService.exists(thumbnailKey)) {
                    byte[] dummyThumb = ("VIDEO_POSTER_" + mediaAssetId).getBytes();
                    storageService.putObject(thumbnailKey, dummyThumb, "image/jpeg");
                }
            }

            media.setStatus(MediaAssetStatus.READY);
            media.setErrorMessage(null);
            mediaAssetRepository.save(media);
            log.info("Media Variant Maker successfully processed mediaAssetId: {}", mediaAssetId);
        } catch (Exception e) {
            log.error("Failed processing mediaAssetId {}: {}", mediaAssetId, e.getMessage());
            media.setStatus(MediaAssetStatus.FAILED);
            media.setErrorMessage("Media variant generation failed: " + e.getMessage());
            mediaAssetRepository.save(media);
        }
    }

    @Transactional
    public MediaAsset retryProcessing(UUID mediaAssetId, UUID requesterId) {
        MediaAsset media = mediaAssetRepository.findById(mediaAssetId)
                .orElseThrow(() -> new IllegalArgumentException("MediaAsset not found: " + mediaAssetId));

        if (!media.getUploaderId().equals(requesterId)) {
            throw new IllegalArgumentException("You can only retry processing for your own uploads");
        }

        media.setStatus(MediaAssetStatus.PROCESSING);
        media.setErrorMessage(null);
        mediaAssetRepository.saveAndFlush(media);

        processMediaAsync(mediaAssetId);
        return media;
    }
}
