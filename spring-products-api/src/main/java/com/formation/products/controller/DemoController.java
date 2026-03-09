package com.formation.products.controller;

import com.formation.products.model.Category;
import com.formation.products.model.Product;
import com.formation.products.model.Supplier;
import com.formation.products.repository.CategoryRepository;
import com.formation.products.repository.ProductRepository;
import com.formation.products.repository.SupplierRepository;
import com.formation.products.service.ProductService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * - POST /api/v1/demo/seed: create Category, Supplier, Product (find-or-create).
 * Verify tables + FK in DB.
 * - POST /api/v1/demo/rollback: save a product then throw → transaction rollback,
 * nothing persisted.
 * - POST /api/v1/demo/test-repositories: consigne 3.4 — create → findById → modify
 * → delete → count for each repo (observe SQL in logs).
 */
@RestController
@RequestMapping("/api/v1/demo")
@Tag(name = "Demo", description = "Seed and demo endpoints for testing")
@SecurityRequirement(name = "bearerAuth")
public class DemoController {

        private static final String ELECTRONICS_CATEGORY_NAME = "Électronique";
        private static final String COUNT_AFTER_SEPARATOR = " after=";

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
        public ResponseEntity<Map<String, Object>> seedDemoData() {
                Category category = categoryRepository.findByName(ELECTRONICS_CATEGORY_NAME)
                                .orElseGet(() -> categoryRepository
                                                .save(new Category(ELECTRONICS_CATEGORY_NAME, "Produits électroniques")));

                String supplierEmail = "contact@techfournisseur.fr";
                Supplier supplier = supplierRepository.findByEmail(supplierEmail)
                                .orElseGet(() -> supplierRepository
                                                .save(new Supplier("TechFournisseur", supplierEmail, "+33123456789")));

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

                long countCatBefore = categoryRepository.count();
                Category createdCategory = categoryRepository.save(new Category("TestCategory_3.4", "Pour test repos"));
                steps.add("Category: created id=" + createdCategory.getId());
                Category foundCategory = categoryRepository.findById(createdCategory.getId()).orElseThrow();
                steps.add("Category: findById ok");
                foundCategory.setDescription("Description modifiée");
                categoryRepository.save(foundCategory);
                steps.add("Category: updated");
                categoryRepository.deleteById(createdCategory.getId());
                steps.add("Category: deleted");
                long countCatAfter = categoryRepository.count();
                steps.add("Category: count before=" + countCatBefore + COUNT_AFTER_SEPARATOR + countCatAfter);

                long countSupBefore = supplierRepository.count();
                Supplier createdSupplier = supplierRepository
                                .save(new Supplier("TestSupplier 3.4", "test-3.4@repo.demo", null));
                steps.add("Supplier: created id=" + createdSupplier.getId());
                Supplier foundSupplier = supplierRepository.findById(createdSupplier.getId()).orElseThrow();
                steps.add("Supplier: findById ok");
                foundSupplier.setPhone("+33000000000");
                supplierRepository.save(foundSupplier);
                steps.add("Supplier: updated");
                supplierRepository.deleteById(createdSupplier.getId());
                steps.add("Supplier: deleted");
                long countSupAfter = supplierRepository.count();
                steps.add("Supplier: count before=" + countSupBefore + COUNT_AFTER_SEPARATOR + countSupAfter);

                Category existingCat = categoryRepository.findByName(ELECTRONICS_CATEGORY_NAME).orElse(null);
                Supplier existingSup = supplierRepository.findByEmail("contact@techfournisseur.fr").orElse(null);
                if (existingCat != null && existingSup != null) {
                        long countProdBefore = productRepository.count();
                        Product createdProduct = new Product("Produit test 3.4", "Desc", new BigDecimal("1.00"), existingCat,
                                        existingSup, 1);
                        createdProduct = productRepository.save(createdProduct);
                        steps.add("Product: created id=" + createdProduct.getId());
                        Product foundProduct = productRepository.findById(createdProduct.getId()).orElseThrow();
                        steps.add("Product: findById ok");
                        foundProduct.setStock(99);
                        productRepository.save(foundProduct);
                        steps.add("Product: updated");
                        productRepository.deleteById(createdProduct.getId());
                        steps.add("Product: deleted");
                        long countProdAfter = productRepository.count();
                        steps.add("Product: count before=" + countProdBefore + COUNT_AFTER_SEPARATOR + countProdAfter);
                } else {
                        steps.add("Product: skipped (run /api/v1/demo/seed first to have category and supplier)");
                }

                report.put("steps", steps);
                report.put("message",
                                "Check server console for Hibernate SQL (insert, select, update, delete, count).");
                return ResponseEntity.ok(report);
        }
}
