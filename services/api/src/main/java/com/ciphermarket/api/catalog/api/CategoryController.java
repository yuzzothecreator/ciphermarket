package com.ciphermarket.api.catalog.api;

import com.ciphermarket.api.catalog.dto.CategoryResponse;
import com.ciphermarket.api.catalog.repository.CategoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "Categories", description = "Product categories")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    @Operation(summary = "List active categories")
    public List<CategoryResponse> listCategories() {
        return categoryRepository.findByActiveTrueOrderBySortOrderAsc().stream()
                .map(CategoryResponse::from)
                .toList();
    }
}
