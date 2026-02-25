package com.formation.products.service;

import com.formation.products.model.Category;
import com.formation.products.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Category> getCategory(Long id) {
        return categoryRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Category> getCategoryWithProducts(Long id) {
        return categoryRepository.findByIdWithProducts(id);
    }

    @Transactional
    public Category createCategory(String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name is required");
        }
        Category category = new Category(name.trim(), description);
        return categoryRepository.save(category);
    }

    @Transactional
    public Category updateCategory(Long id, String name, String description) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));
        if (name != null && !name.trim().isEmpty()) {
            existing.setName(name.trim());
        }
        if (description != null) {
            existing.setDescription(description);
        }
        return categoryRepository.save(existing);
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new IllegalArgumentException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }
}
