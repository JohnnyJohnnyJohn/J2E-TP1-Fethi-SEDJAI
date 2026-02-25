package com.formation.products.exception;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(Long id) {
        super("Catégorie non trouvée avec l'ID: " + id);
    }
}
