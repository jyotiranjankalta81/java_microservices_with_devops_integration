package com.jobintel.evidencing.repository;

import com.jobintel.evidencing.domain.entity.PublicationDlqEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PublicationDlqEntryRepository extends JpaRepository<PublicationDlqEntry, UUID> {
}
