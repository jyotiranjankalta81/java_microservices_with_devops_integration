package com.jobintel.evidencing.repository;

import com.jobintel.evidencing.domain.entity.Post;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID>, JpaSpecificationExecutor<Post> {

    Optional<Post> findByIdempotencyKey(String idempotencyKey);

    @Override
    @EntityGraph(attributePaths = {"school", "branch", "schoolClass", "category"})
    List<Post> findAll();
}
