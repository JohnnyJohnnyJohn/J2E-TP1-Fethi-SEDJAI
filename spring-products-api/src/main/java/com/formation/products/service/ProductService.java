package com.formation.products.service;

import com.formation.products.model.Product;
import com.formation.products.repository.IProductRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final IProductRepository productRepository;

    public ProductService(IProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product createProduct(Product product) {
        validateProduct(product);
        return productRepository.save(product);
    }

    public Optional<Product> getProduct(String id) {
        return productRepository.findById(id);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    public Product updateProduct(String id, Product updatedProduct) {
        if (!productRepository.exists(id)) {
            throw new IllegalArgumentException("Product not found with id: " + id);
        }

        validateProduct(updatedProduct);
        updatedProduct.setId(id);

        productRepository.findById(id).ifPresent(existing ->
                updatedProduct.setCreatedAt(existing.getCreatedAt())
        );

        return productRepository.save(updatedProduct);
    }

    public void deleteProduct(String id) {
        productRepository.delete(id);
    }

    public void updateStock(String id, int quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));

        int newStock = product.getStock() + quantity;
        if (newStock < 0) {
            throw new IllegalArgumentException("Insufficient stock. Current: " + product.getStock() + ", requested change: " + quantity);
        }

        product.setStock(newStock);
        productRepository.save(product);
    }

    private void validateProduct(Product product) {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Product price must be positive");
        }
        if (product.getCategory() == null || product.getCategory().trim().isEmpty()) {
            throw new IllegalArgumentException("Product category is required");
        }
        if (product.getStock() < 0) {
            throw new IllegalArgumentException("Product stock cannot be negative");
        }
    }
}
