package com.formation.products.service;

import com.formation.products.exception.DuplicateProductException;
import com.formation.products.exception.InsufficientStockException;
import com.formation.products.exception.ProductNotFoundException;
import com.formation.products.model.Category;
import com.formation.products.model.Product;
import com.formation.products.repository.CategoryRepository;
import com.formation.products.repository.ProductRepository;
import com.formation.products.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(readOnly = true)
    public List<Product> getAllProductsSlow() {
        List<Product> products = productRepository.findAllSlow();
        products.forEach(p -> {
            if (p.getCategory() != null) p.getCategory().getName();
            if (p.getSupplier() != null) p.getSupplier().getName();
        });
        return products;
    }

    @Transactional(readOnly = true)
    public List<Product> getAllProductsFast() {
        return productRepository.findAllWithCategoryAndSupplier();
    }

    @Transactional(readOnly = true)
    public List<Product> getAllProductsWithCategoryGraph() {
        return productRepository.findAllWithCategoryGraph();
    }

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
        if (product.getSku() != null && productRepository.existsBySku(product.getSku())) {
            throw new DuplicateProductException(product.getSku());
        }
        return productRepository.save(product);
    }

    @Transactional
    public Product createProductWithCategory(Product product, String categoryName) {
        Category category = categoryRepository.findByName(categoryName)
                .orElseGet(() -> categoryRepository.save(new Category(categoryName, null)));
        product.setCategory(category);
        if (product.getSku() != null && productRepository.existsBySku(product.getSku())) {
            throw new DuplicateProductException(product.getSku());
        }
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, Product updatedProduct) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        updatedProduct.setId(id);
        updatedProduct.setCreatedAt(existing.getCreatedAt());
        return productRepository.save(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
    }

    @Transactional
    public void updateStock(Long id, int quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        int newStock = product.getStock() + quantity;
        if (newStock < 0) {
            throw new InsufficientStockException(product.getName(), Math.abs(quantity), product.getStock());
        }
        product.setStock(newStock);
        productRepository.save(product);
    }

    @Transactional
    public void decreaseStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        if (product.getStock() < quantity) {
            throw new InsufficientStockException(product.getName(), quantity, product.getStock());
        }
        product.setStock(product.getStock() - quantity);
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

    @Transactional
    public void createProductThenRollback(Product product) {
        productRepository.save(product);
        throw new RuntimeException("Rollback demo: product was not persisted");
    }
}
