package com.formation.products.exception;

public class SupplierNotFoundException extends RuntimeException {
    public SupplierNotFoundException(Long id) {
        super("Fournisseur non trouvé avec l'ID: " + id);
    }
}
