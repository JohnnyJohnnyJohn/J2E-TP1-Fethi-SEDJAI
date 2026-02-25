package com.formation.products.controller;

import com.formation.products.exception.ProductNotFoundException;
import com.formation.products.model.Product;
import com.formation.products.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

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
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        return productService.getProduct(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
        Product created = productService.createProduct(product);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id,
                                                 @Valid @RequestBody Product product) {
        Product updated = productService.updateProduct(id, product);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<Product> updateStock(@PathVariable Long id,
                                               @RequestBody StockUpdate stockUpdate) {
        productService.updateStock(id, stockUpdate.getQuantity());
        return productService.getProduct(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @PatchMapping("/{id}/decrease-stock")
    public ResponseEntity<Void> decreaseStock(@PathVariable Long id,
                                              @RequestParam int quantity) {
        productService.decreaseStock(id, quantity);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/slow")
    public ResponseEntity<List<Product>> getProductsSlow() {
        return ResponseEntity.ok(productService.getAllProductsSlow());
    }

    @GetMapping("/fast")
    public ResponseEntity<List<Product>> getProductsFast() {
        return ResponseEntity.ok(productService.getAllProductsFast());
    }

    @GetMapping("/graph/category")
    public ResponseEntity<List<Product>> getProductsWithCategoryGraph() {
        return ResponseEntity.ok(productService.getAllProductsWithCategoryGraph());
    }

    @GetMapping("/graph/full")
    public ResponseEntity<List<Product>> getProductsWithFullGraph() {
        return ResponseEntity.ok(productService.getAllProductsWithFullGraph());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    public static class StockUpdate {
        private int quantity;
        public StockUpdate() {}
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
}
