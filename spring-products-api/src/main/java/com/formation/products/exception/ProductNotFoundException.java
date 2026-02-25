package com.formation.products.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(Long id) {
        super("Produit non trouvé avec l'ID: " + id);
    }
}
