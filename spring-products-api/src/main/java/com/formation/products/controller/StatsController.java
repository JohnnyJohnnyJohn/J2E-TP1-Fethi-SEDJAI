package com.formation.products.controller;

import com.formation.products.dto.CategoryStats;
import com.formation.products.dto.MostOrderedProduct;
import com.formation.products.dto.OrderStatusCount;
import com.formation.products.model.Category;
import com.formation.products.model.Product;
import com.formation.products.service.StatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Part 6 – JPQL aggregation endpoints for Thunder Client / Livrable 6.
 */
@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    /** Nombre de produits par catégorie (raw Object[]) */
    @GetMapping("/products-by-category/count")
    public ResponseEntity<List<Map<String, Object>>> countByCategory() {
        List<Object[]> rows = statsService.getCountByCategory();
        List<Map<String, Object>> result = rows.stream()
                .map(row -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("categoryName", row[0]);
                    m.put("productCount", row[1]);
                    return m;
                })
                .toList();
        return ResponseEntity.ok(result);
    }

    /** Prix moyen par catégorie (raw Object[]) */
    @GetMapping("/products-by-category/average-price")
    public ResponseEntity<List<Map<String, Object>>> averagePriceByCategory() {
        List<Object[]> rows = statsService.getAveragePriceByCategory();
        List<Map<String, Object>> result = rows.stream()
                .map(row -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("categoryName", row[0]);
                    m.put("averagePrice", row[1]);
                    return m;
                })
                .toList();
        return ResponseEntity.ok(result);
    }

    /** DTO projection: CategoryStats */
    @GetMapping("/category-stats")
    public ResponseEntity<List<CategoryStats>> categoryStats() {
        return ResponseEntity.ok(statsService.getCategoryStats());
    }

    /** Top N produits les plus chers */
    @GetMapping("/top-expensive")
    public ResponseEntity<List<Map<String, Object>>> topExpensive(
            @RequestParam(defaultValue = "10") int limit) {
        List<Product> products = statsService.getTopExpensive(limit);
        List<Map<String, Object>> result = products.stream()
                .map(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", p.getId());
                    m.put("name", p.getName());
                    m.put("price", p.getPrice());
                    m.put("category", p.getCategory() != null ? p.getCategory().getName() : null);
                    return m;
                })
                .toList();
        return ResponseEntity.ok(result);
    }

    /** Produits jamais commandés */
    @GetMapping("/never-ordered-products")
    public ResponseEntity<List<Map<String, Object>>> neverOrderedProducts() {
        List<Product> products = statsService.getNeverOrderedProducts();
        List<Map<String, Object>> result = products.stream()
                .map(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", p.getId());
                    m.put("name", p.getName());
                    m.put("price", p.getPrice());
                    return m;
                })
                .toList();
        return ResponseEntity.ok(result);
    }

    /** Catégories avec au moins N produits */
    @GetMapping("/categories-min-products")
    public ResponseEntity<List<Map<String, Object>>> categoriesWithMinProducts(
            @RequestParam(defaultValue = "1") int min) {
        List<Category> categories = statsService.getCategoriesWithMinProducts(min);
        List<Map<String, Object>> result = categories.stream()
                .map(c -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", c.getId());
                    m.put("name", c.getName());
                    return m;
                })
                .toList();
        return ResponseEntity.ok(result);
    }

    /** Chiffre d'affaires total (commandes DELIVERED) */
    @GetMapping("/total-revenue")
    public ResponseEntity<Map<String, Object>> totalRevenue() {
        return ResponseEntity.ok(Map.of(
                "status", "DELIVERED",
                "totalRevenue", statsService.getTotalRevenue()));
    }

    /** Nombre de commandes par statut (DTO) */
    @GetMapping("/orders-by-status")
    public ResponseEntity<List<OrderStatusCount>> countByStatus() {
        return ResponseEntity.ok(statsService.getCountByStatus());
    }

    /** Produits les plus commandés */
    @GetMapping("/most-ordered-products")
    public ResponseEntity<List<MostOrderedProduct>> mostOrderedProducts(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(statsService.getMostOrderedProducts(limit));
    }
}
