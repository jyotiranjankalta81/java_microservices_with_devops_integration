package com.jobintel.evidencing.repository;

import com.jobintel.evidencing.domain.entity.PostCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostCategoryRepository extends JpaRepository<PostCategory, UUID> {

    List<PostCategory> findByActiveTrueOrderByDisplayOrderAsc();

    List<PostCategory> findAllByOrderByDisplayOrderAsc();

    Optional<PostCategory> findByNameIgnoreCase(String name);
}
