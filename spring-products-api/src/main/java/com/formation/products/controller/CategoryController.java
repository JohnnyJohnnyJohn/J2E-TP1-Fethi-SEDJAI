package com.formation.products.controller;

import com.formation.products.model.Category;
import com.formation.products.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private static final String NOT_FOUND = "not found";

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getCategory(@PathVariable Long id,
                                              @RequestParam(defaultValue = "false") boolean withProducts) {
        if (withProducts) {
            return categoryService.getCategoryWithProducts(id)
                    .<ResponseEntity<Object>>map(ResponseEntity::ok)
                    .orElse(ResponseEntity.status(404).body(Map.of("message", "Category not found with id: " + id)));
        }
        return categoryService.getCategory(id)
                .<ResponseEntity<Object>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(404).body(Map.of("message", "Category not found with id: " + id)));
    }

    @PostMapping
    public ResponseEntity<Object> createCategory(@RequestBody CategoryCreateRequest request) {
        try {
            Category created = categoryService.createCategory(request.getName(), request.getDescription());
            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(created.getId())
                    .toUri();
            return ResponseEntity.created(location).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateCategory(@PathVariable Long id, @RequestBody CategoryCreateRequest request) {
        try {
            Category updated = categoryService.updateCategory(id, request.getName(), request.getDescription());
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().contains(NOT_FOUND)) {
                return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
            }
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().contains(NOT_FOUND)) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    public static class CategoryCreateRequest {
        private String name;
        private String description;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
