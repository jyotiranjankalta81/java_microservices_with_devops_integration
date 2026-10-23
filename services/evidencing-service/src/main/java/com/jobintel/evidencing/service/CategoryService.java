package com.jobintel.evidencing.service;

import com.jobintel.evidencing.domain.entity.PostCategory;
import com.jobintel.evidencing.dto.PostCategoryCreateRequest;
import com.jobintel.evidencing.dto.PostCategoryResponse;
import com.jobintel.evidencing.dto.PostCategoryUpdateRequest;
import com.jobintel.evidencing.exception.CategoryInactiveException;
import com.jobintel.evidencing.repository.PostCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final PostCategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<PostCategoryResponse> getActiveCategories() {
        return categoryRepository.findByActiveTrueOrderByDisplayOrderAsc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PostCategoryResponse> getAllCategories() {
        return categoryRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PostCategory getActiveCategoryById(UUID id) {
        PostCategory cat = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post category not found: " + id));
        if (!cat.isActive()) {
            throw new CategoryInactiveException("Post category '" + cat.getName() + "' is inactive and cannot be selected for new posts.");
        }
        return cat;
    }

    @Transactional
    public PostCategoryResponse createCategory(PostCategoryCreateRequest request) {
        categoryRepository.findByNameIgnoreCase(request.getName()).ifPresent(c -> {
            throw new IllegalArgumentException("Category with name '" + request.getName() + "' already exists.");
        });

        PostCategory category = PostCategory.builder()
                .name(request.getName())
                .description(request.getDescription())
                .icon(request.getIcon() != null ? request.getIcon() : "tag")
                .active(true)
                .displayOrder(request.getDisplayOrder())
                .build();

        return mapToResponse(categoryRepository.save(category));
    }

    @Transactional
    public PostCategoryResponse updateCategory(UUID id, PostCategoryUpdateRequest request) {
        PostCategory cat = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + id));

        cat.setName(request.getName());
        if (request.getDescription() != null) cat.setDescription(request.getDescription());
        if (request.getIcon() != null) cat.setIcon(request.getIcon());
        if (request.getActive() != null) cat.setActive(request.getActive());
        if (request.getDisplayOrder() != null) cat.setDisplayOrder(request.getDisplayOrder());

        return mapToResponse(categoryRepository.save(cat));
    }

    private PostCategoryResponse mapToResponse(PostCategory cat) {
        return PostCategoryResponse.builder()
                .id(cat.getId())
                .name(cat.getName())
                .description(cat.getDescription())
                .icon(cat.getIcon())
                .active(cat.isActive())
                .displayOrder(cat.getDisplayOrder())
                .build();
    }
}
