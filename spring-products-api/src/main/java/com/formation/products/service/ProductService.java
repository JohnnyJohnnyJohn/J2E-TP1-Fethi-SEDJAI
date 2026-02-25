package com.formation.products.service;

import com.formation.products.model.Category;
import com.formation.products.model.Product;
import com.formation.products.repository.CategoryRepository;
import com.formation.products.repository.ProductRepository;
import com.formation.products.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          SupplierRepository supplierRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
    }

    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAllWithCategoryAndSupplier();
    }

    /** Part 7: N+1 demo — no JOIN FETCH, triggers N extra SELECTs for category/supplier */
    @Transactional(readOnly = true)
    public List<Product> getAllProductsSlow() {
        List<Product> products = productRepository.findAllSlow();
        products.forEach(p -> {
            if (p.getCategory() != null) p.getCategory().getName();
            if (p.getSupplier() != null) p.getSupplier().getName();
        });
        return products;
    }

    /** Part 7: optimized with JOIN FETCH — single query */
    @Transactional(readOnly = true)
    public List<Product> getAllProductsFast() {
        return productRepository.findAllWithCategoryAndSupplier();
    }

    /** Part 7.3: using @NamedEntityGraph("Product.withCategory") */
    @Transactional(readOnly = true)
    public List<Product> getAllProductsWithCategoryGraph() {
        return productRepository.findAllWithCategoryGraph();
    }

    /** Part 7.3: using @NamedEntityGraph("Product.full") */
    @Transactional(readOnly = true)
    public List<Product> getAllProductsWithFullGraph() {
        return productRepository.findAllWithFullGraph();
    }

    @Transactional(readOnly = true)
    public Optional<Product> getProduct(Long id) {
        return productRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsByCategoryId(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsByCategoryName(String categoryName) {
        return categoryRepository.findByName(categoryName)
                .map(productRepository::findByCategory)
                .orElse(List.of());
    }

    @Transactional
    public Product createProduct(Product product) {
        validateProduct(product);
        return productRepository.save(product);
    }

    @Transactional
    public Product createProductWithCategory(Product product, String categoryName) {
        Category category = categoryRepository.findByName(categoryName)
                .orElseGet(() -> categoryRepository.save(new Category(categoryName, null)));
        product.setCategory(category);
        validateProduct(product);
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, Product updatedProduct) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
        validateProduct(updatedProduct);
        updatedProduct.setId(id);
        updatedProduct.setCreatedAt(existing.getCreatedAt());
        return productRepository.save(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    @Transactional
    public void updateStock(Long id, int quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
        int newStock = product.getStock() + quantity;
        if (newStock < 0) {
            throw new IllegalArgumentException("Insufficient stock. Current: " + product.getStock() + ", requested change: " + quantity);
        }
        product.setStock(newStock);
        productRepository.save(product);
    }

    @Transactional
    public void transferProducts(Long fromCategoryId, Long toCategoryId) {
        Category from = categoryRepository.findById(fromCategoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + fromCategoryId));
        Category to = categoryRepository.findById(toCategoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + toCategoryId));
        List<Product> products = productRepository.findByCategory(from);
        for (Product p : products) {
            p.setCategory(to);
            productRepository.save(p);
        }
    }

    /**
     * Demo: creates a product then throws so the transaction rolls back.
     * Use from a test or temporary endpoint to verify nothing is persisted.
     */
    @Transactional
    public void createProductThenRollback(Product product) {
        validateProduct(product);
        productRepository.save(product);
        throw new RuntimeException("Rollback demo: product was not persisted");
    }

    private void validateProduct(Product product) {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Product price must be positive");
        }
        if (product.getStock() < 0) {
            throw new IllegalArgumentException("Product stock cannot be negative");
        }
    }
}
