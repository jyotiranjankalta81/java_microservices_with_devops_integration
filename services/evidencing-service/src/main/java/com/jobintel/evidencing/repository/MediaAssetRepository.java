package com.jobintel.evidencing.repository;

import com.jobintel.evidencing.domain.entity.MediaAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface MediaAssetRepository extends JpaRepository<MediaAsset, UUID> {

    List<MediaAsset> findByIdIn(Collection<UUID> ids);

    List<MediaAsset> findByUploaderId(UUID uploaderId);
}
