package com.formation.products.controller;

import com.formation.products.model.Category;
import com.formation.products.model.Product;
import com.formation.products.model.Supplier;
import com.formation.products.repository.CategoryRepository;
import com.formation.products.repository.ProductRepository;
import com.formation.products.repository.SupplierRepository;
import com.formation.products.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Temporary controller for TP2 demos.
 * - POST /api/demo/seed: create Category, Supplier, Product (find-or-create).
 * Verify tables + FK in DB.
 * - POST /api/demo/rollback: save a product then throw → transaction rollback,
 * nothing persisted.
 * - POST /api/demo/test-repositories: consigne 3.4 — create → findById → modify
 * → delete → count for each repo (observe SQL in logs).
 */
@RestController
@RequestMapping("/api/demo")
public class DemoController {

        private final ProductService productService;
        private final CategoryRepository categoryRepository;
        private final SupplierRepository supplierRepository;
        private final ProductRepository productRepository;

        public DemoController(ProductService productService,
                        CategoryRepository categoryRepository,
                        SupplierRepository supplierRepository,
                        ProductRepository productRepository) {
                this.productService = productService;
                this.categoryRepository = categoryRepository;
                this.supplierRepository = supplierRepository;
                this.productRepository = productRepository;
        }

        @PostMapping("/seed")
        public ResponseEntity<Map<String, Object>> seed() {
                // 1. Find or create Category
                Category category = categoryRepository.findByName("Électronique")
                                .orElseGet(() -> categoryRepository
                                                .save(new Category("Électronique", "Produits électroniques")));

                // 2. Find or create Supplier
                String supplierEmail = "contact@techfournisseur.fr";
                Supplier supplier = supplierRepository.findByEmail(supplierEmail)
                                .orElseGet(() -> supplierRepository
                                                .save(new Supplier("TechFournisseur", supplierEmail, "+33123456789")));

                // 3. Create Product linked to both, then save
                Product product = new Product(
                                "Ordinateur portable",
                                "PC 15 pouces, 16 Go RAM",
                                new BigDecimal("899.99"),
                                category,
                                supplier,
                                10);
                product = productRepository.save(product);

                return ResponseEntity.ok(Map.of(
                                "category",
                                Map.of("id", category.getId(), "name", category.getName(), "description",
                                                category.getDescription()),
                                "supplier",
                                Map.of("id", supplier.getId(), "name", supplier.getName(), "email", supplier.getEmail(),
                                                "phone", supplier.getPhone()),
                                "product", Map.of("id", product.getId(), "name", product.getName(), "categoryId",
                                                category.getId(), "supplierId", supplier.getId())));
        }

        @PostMapping("/rollback")
        public ResponseEntity<Void> triggerRollback(@RequestBody Product product) {
                productService.createProductThenRollback(product);
                return ResponseEntity.ok().build();
        }

        /**
         * Consigne 3.4: test each repository — create, findById, modify, delete, count.
         * Run this then capture the server console: Hibernate will log all SQL (insert,
         * select, update, delete, count).
         */
        @PostMapping("/test-repositories")
        public ResponseEntity<Map<String, Object>> testRepositories() {
                Map<String, Object> report = new LinkedHashMap<>();
                List<String> steps = new ArrayList<>();

                // ---- CategoryRepository ----
                long countCatBefore = categoryRepository.count();
                Category cat = categoryRepository.save(new Category("TestCategory_3.4", "Pour test repos"));
                steps.add("Category: created id=" + cat.getId());
                Category catFound = categoryRepository.findById(cat.getId()).orElseThrow();
                steps.add("Category: findById ok");
                catFound.setDescription("Description modifiée");
                categoryRepository.save(catFound);
                steps.add("Category: updated");
                categoryRepository.deleteById(cat.getId());
                steps.add("Category: deleted");
                long countCatAfter = categoryRepository.count();
                steps.add("Category: count before=" + countCatBefore + " after=" + countCatAfter);

                // ---- SupplierRepository ----
                long countSupBefore = supplierRepository.count();
                Supplier sup = supplierRepository.save(new Supplier("TestSupplier 3.4", "test-3.4@repo.demo", null));
                steps.add("Supplier: created id=" + sup.getId());
                Supplier supFound = supplierRepository.findById(sup.getId()).orElseThrow();
                steps.add("Supplier: findById ok");
                supFound.setPhone("+33000000000");
                supplierRepository.save(supFound);
                steps.add("Supplier: updated");
                supplierRepository.deleteById(sup.getId());
                steps.add("Supplier: deleted");
                long countSupAfter = supplierRepository.count();
                steps.add("Supplier: count before=" + countSupBefore + " after=" + countSupAfter);

                // ---- ProductRepository (use existing category/supplier from seed) ----
                Category existingCat = categoryRepository.findByName("Électronique").orElse(null);
                Supplier existingSup = supplierRepository.findByEmail("contact@techfournisseur.fr").orElse(null);
                if (existingCat != null && existingSup != null) {
                        long countProdBefore = productRepository.count();
                        Product prod = new Product("Produit test 3.4", "Desc", new BigDecimal("1.00"), existingCat,
                                        existingSup, 1);
                        prod = productRepository.save(prod);
                        steps.add("Product: created id=" + prod.getId());
                        Product prodFound = productRepository.findById(prod.getId()).orElseThrow();
                        steps.add("Product: findById ok");
                        prodFound.setStock(99);
                        productRepository.save(prodFound);
                        steps.add("Product: updated");
                        productRepository.deleteById(prod.getId());
                        steps.add("Product: deleted");
                        long countProdAfter = productRepository.count();
                        steps.add("Product: count before=" + countProdBefore + " after=" + countProdAfter);
                } else {
                        steps.add("Product: skipped (run /api/demo/seed first to have category and supplier)");
                }

                report.put("steps", steps);
                report.put("message",
                                "Check server console for Hibernate SQL (insert, select, update, delete, count).");
                return ResponseEntity.ok(report);
        }
}
