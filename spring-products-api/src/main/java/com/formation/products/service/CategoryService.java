package com.formation.products.service;

import com.formation.products.exception.CategoryNotEmptyException;
import com.formation.products.exception.CategoryNotFoundException;
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
            throw new IllegalArgumentException("Le nom de la catégorie est obligatoire");
        }
        Category category = new Category(name.trim(), description);
        return categoryRepository.save(category);
    }

    @Transactional
    public Category updateCategory(Long id, String name, String description) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
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
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        if (!category.getProducts().isEmpty()) {
            throw new CategoryNotEmptyException(
                    "Impossible de supprimer une catégorie contenant des produits");
        }

        categoryRepository.delete(category);
    }
}
