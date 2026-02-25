package com.formation.products.controller;

import com.formation.products.exception.CategoryNotFoundException;
import com.formation.products.model.Category;
import com.formation.products.service.CategoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategory(@PathVariable Long id,
                                                @RequestParam(defaultValue = "false") boolean withProducts) {
        if (withProducts) {
            return categoryService.getCategoryWithProducts(id)
                    .map(ResponseEntity::ok)
                    .orElseThrow(() -> new CategoryNotFoundException(id));
        }
        return categoryService.getCategory(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    @PostMapping
    public ResponseEntity<Category> createCategory(@Valid @RequestBody CategoryCreateRequest request) {
        Category created = categoryService.createCategory(request.getName(), request.getDescription());
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id,
                                                   @Valid @RequestBody CategoryCreateRequest request) {
        Category updated = categoryService.updateCategory(id, request.getName(), request.getDescription());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    public static class CategoryCreateRequest {
        @NotBlank(message = "Le nom de la catégorie est obligatoire")
        @Size(min = 2, max = 100, message = "Le nom doit contenir entre {min} et {max} caractères")
        private String name;

        @Size(max = 500, message = "La description ne peut pas dépasser {max} caractères")
        private String description;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
