package com.formation.products.controller;

import com.formation.products.model.Product;
import com.formation.products.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final String NOT_FOUND = "not found";

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String category) {

        List<Product> products;
        if (categoryId != null) {
            products = productService.getProductsByCategoryId(categoryId);
        } else if (category != null && !category.trim().isEmpty()) {
            products = productService.getProductsByCategoryName(category);
        } else {
            products = productService.getAllProducts();
        }
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getProduct(@PathVariable Long id) {
        return productService.getProduct(id)
                .<ResponseEntity<Object>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(404)
                        .body(new ErrorMessage("Product not found with id: " + id)));
    }

    @PostMapping
    public ResponseEntity<Object> createProduct(@RequestBody Product product) {
        try {
            Product created = productService.createProduct(product);
            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(created.getId())
                    .toUri();
            return ResponseEntity.created(location).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorMessage(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        try {
            Product updated = productService.updateProduct(id, product);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().contains(NOT_FOUND)) {
                return ResponseEntity.status(404).body(new ErrorMessage(e.getMessage()));
            }
            return ResponseEntity.badRequest().body(new ErrorMessage(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<Object> updateStock(@PathVariable Long id, @RequestBody StockUpdate stockUpdate) {
        try {
            productService.updateStock(id, stockUpdate.getQuantity());
            return productService.getProduct(id)
                    .<ResponseEntity<Object>>map(ResponseEntity::ok)
                    .orElse(ResponseEntity.status(404).body(new ErrorMessage("Product not found with id: " + id)));
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().contains(NOT_FOUND)) {
                return ResponseEntity.status(404).body(new ErrorMessage(e.getMessage()));
            }
            return ResponseEntity.badRequest().body(new ErrorMessage(e.getMessage()));
        }
    }

    /** Part 7.1: N+1 — no JOIN FETCH, observe SQL logs */
    @GetMapping("/slow")
    public ResponseEntity<List<Product>> getProductsSlow() {
        return ResponseEntity.ok(productService.getAllProductsSlow());
    }

    /** Part 7.1: optimized — single query with JOIN FETCH */
    @GetMapping("/fast")
    public ResponseEntity<List<Product>> getProductsFast() {
        return ResponseEntity.ok(productService.getAllProductsFast());
    }

    /** Part 7.3: using @NamedEntityGraph("Product.withCategory") */
    @GetMapping("/graph/category")
    public ResponseEntity<List<Product>> getProductsWithCategoryGraph() {
        return ResponseEntity.ok(productService.getAllProductsWithCategoryGraph());
    }

    /** Part 7.3: using @NamedEntityGraph("Product.full") */
    @GetMapping("/graph/full")
    public ResponseEntity<List<Product>> getProductsWithFullGraph() {
        return ResponseEntity.ok(productService.getAllProductsWithFullGraph());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().contains(NOT_FOUND)) {
                return ResponseEntity.notFound().build();
            }
            throw e;
        }
    }

    public static class ErrorMessage {
        private String message;

        /** Default constructor for JSON serialization. */
        public ErrorMessage() {}

        public ErrorMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public static class StockUpdate {
        private int quantity;

        /** Default constructor for JSON deserialization. */
        public StockUpdate() {}

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }
}
