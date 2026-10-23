package com.jobintel.evidencing.web;

import com.jobintel.evidencing.dto.PostCategoryCreateRequest;
import com.jobintel.evidencing.dto.PostCategoryResponse;
import com.jobintel.evidencing.dto.PostCategoryUpdateRequest;
import com.jobintel.evidencing.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/evidencing/categories")
@RequiredArgsConstructor
@Tag(name = "Evidencing Categories", description = "Endpoints for managing post categories/domains")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Get active categories/domains for post creation and filtering")
    public List<PostCategoryResponse> getActiveCategories() {
        return categoryService.getActiveCategories();
    }

    @GetMapping("/all")
    @Operation(summary = "Get all categories including deactivated ones (for admin)")
    public List<PostCategoryResponse> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new post category/domain")
    public PostCategoryResponse createCategory(@Valid @RequestBody PostCategoryCreateRequest request) {
        return categoryService.createCategory(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update or deactivate/activate a post category")
    public PostCategoryResponse updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody PostCategoryUpdateRequest request) {
        return categoryService.updateCategory(id, request);
    }
}
