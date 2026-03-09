package com.formation.products.service;

import com.formation.products.exception.CategoryNotFoundException;
import com.formation.products.exception.DuplicateProductException;
import com.formation.products.exception.InsufficientStockException;
import com.formation.products.exception.ProductNotFoundException;
import com.formation.products.exception.SupplierNotFoundException;
import com.formation.products.model.Category;
import com.formation.products.model.Product;
import com.formation.products.model.Supplier;
import com.formation.products.repository.CategoryRepository;
import com.formation.products.repository.ProductRepository;
import com.formation.products.repository.SupplierRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    /**
     * Returns a paginated list of products ordered by creation date descending.
     * Uses the "Product.full" entity graph to preload related category and supplier.
     */
    @Transactional(readOnly = true)
    public Page<Product> getAllProducts(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return productRepository.findAll(pageRequest);
    }

    @Transactional(readOnly = true)
    public List<Product> getAllProductsSlow() {
        List<Product> products = productRepository.findAllSlow();
        products.forEach(p -> {
            if (p.getCategory() != null)
                p.getCategory().getName();
            if (p.getSupplier() != null)
                p.getSupplier().getName();
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
        return productRepository.findByIdWithCategoryAndSupplier(id);
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsByCategoryId(Long categoryId) {
        return productRepository.findByCategoryIdWithCategoryAndSupplier(categoryId);
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsByCategoryName(String categoryName) {
        return categoryRepository.findByName(categoryName)
                .map(productRepository::findByCategoryWithCategoryAndSupplier)
                .orElse(List.of());
    }

    /**
     * Creates a product after validating business constraints (existing category/supplier
     * and unique SKU), then returns a fully hydrated entity for serialization.
     */
    @Transactional
    public Product createProduct(Product product) {
        String normalizedSku = normalizeSku(product.getSku());
        product.setSku(normalizedSku);
        if (normalizedSku != null && productRepository.existsBySku(normalizedSku)) {
            throw new DuplicateProductException(normalizedSku);
        }
        normalizeRelations(product);
        Product created = productRepository.save(product);
        return productRepository.findByIdWithCategoryAndSupplier(created.getId()).orElse(created);
    }

    @Transactional
    public Product createProductWithCategory(Product product, String categoryName) {
        String normalizedSku = normalizeSku(product.getSku());
        product.setSku(normalizedSku);
        if (normalizedSku != null && productRepository.existsBySku(normalizedSku)) {
            throw new DuplicateProductException(normalizedSku);
        }
        Category category = categoryRepository.findByName(categoryName)
                .orElseGet(() -> categoryRepository.save(new Category(categoryName, null)));
        product.setCategory(category);
        normalizeSupplier(product);
        Product created = productRepository.save(product);
        return productRepository.findByIdWithCategoryAndSupplier(created.getId()).orElse(created);
    }

    /**
     * Updates a product while preserving immutable audit data and enforcing SKU uniqueness.
     * The returned instance is reloaded with relations to avoid lazy-loading issues.
     */
    @Transactional
    public Product updateProduct(Long id, Product updatedProduct) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        String normalizedSku = normalizeSku(updatedProduct.getSku());
        if (normalizedSku != null && productRepository.existsBySkuAndIdNot(normalizedSku, id)) {
            throw new DuplicateProductException(normalizedSku);
        }
        Category resolvedCategory = resolveCategory(updatedProduct);
        Supplier resolvedSupplier = resolveSupplier(updatedProduct);

        // Update the managed entity instead of persisting detached request body.
        existing.setName(updatedProduct.getName());
        existing.setDescription(updatedProduct.getDescription());
        existing.setPrice(updatedProduct.getPrice());
        existing.setStock(updatedProduct.getStock());
        existing.setSku(normalizedSku);
        existing.setCategory(resolvedCategory);
        existing.setSupplier(resolvedSupplier);

        Product saved = productRepository.save(existing);
        return productRepository.findByIdWithCategoryAndSupplier(saved.getId()).orElse(saved);
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

    /**
     * Decreases stock for a product and fails fast when the available quantity is insufficient.
     */
    @Transactional
    public void decreaseStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        if (product.getStock() < quantity) {
            throw new InsufficientStockException(product.getName(), quantity, product.getStock());
        }
        product.setStock(product.getStock() - quantity);
    }

    /**
     * Transfers every product from one category to another in a single transaction.
     */
    @Transactional
    public void transferProducts(Long fromCategoryId, Long toCategoryId) {
        Category from = categoryRepository.findById(fromCategoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + fromCategoryId));
        Category to = categoryRepository.findById(toCategoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + toCategoryId));
        List<Product> products = productRepository.findByCategory(from);
        for (Product product : products) {
            product.setCategory(to);
            productRepository.save(product);
        }
    }

    @Transactional
    public void createProductThenRollback(Product product) {
        normalizeRelations(product);
        productRepository.save(product);
        throw new IllegalStateException("Rollback demo: product was not persisted");
    }

    private void normalizeRelations(Product product) {
        normalizeCategory(product);
        normalizeSupplier(product);
    }

    private void normalizeCategory(Product product) {
        product.setCategory(resolveCategory(product));
    }

    private void normalizeSupplier(Product product) {
        product.setSupplier(resolveSupplier(product));
    }

    private String normalizeSku(String sku) {
        if (sku == null) {
            return null;
        }
        return sku.trim();
    }

    private Category resolveCategory(Product product) {
        if (product.getCategory() == null || product.getCategory().getId() == null) {
            throw new IllegalArgumentException("category.id is required");
        }
        Long categoryId = product.getCategory().getId();
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));
    }

    private Supplier resolveSupplier(Product product) {
        if (product.getSupplier() == null) {
            return null;
        }
        if (product.getSupplier().getId() == null) {
            throw new IllegalArgumentException("supplier.id is required when supplier is provided");
        }
        Long supplierId = product.getSupplier().getId();
        return supplierRepository.findById(supplierId)
                .orElseThrow(() -> new SupplierNotFoundException(supplierId));
    }
}
