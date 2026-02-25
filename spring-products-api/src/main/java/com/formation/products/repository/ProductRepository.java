package com.formation.products.repository;

import com.formation.products.dto.CategoryStats;
import com.formation.products.model.Category;
import com.formation.products.model.Product;
import com.formation.products.model.Supplier;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.supplier WHERE p.id = :id")
    Optional<Product> findByIdWithCategoryAndSupplier(@Param("id") Long id);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.supplier ORDER BY p.createdAt DESC")
    List<Product> findAllWithCategoryAndSupplier();

    List<Product> findByCategory(Category category);

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findBySupplier(Supplier supplier);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.supplier WHERE p.price BETWEEN :min AND :max ORDER BY p.price")
    List<Product> findByPriceBetween(@Param("min") BigDecimal min, @Param("max") BigDecimal max);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.supplier WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> searchByName(@Param("keyword") String keyword);

    // --- Part 6: Aggregation ---

    @Query("SELECT p.category.name, COUNT(p) FROM Product p GROUP BY p.category")
    List<Object[]> countByCategory();

    @Query("SELECT p.category.name, AVG(p.price) FROM Product p GROUP BY p.category")
    List<Object[]> averagePriceByCategory();

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.supplier ORDER BY p.price DESC")
    List<Product> findTopExpensive(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p NOT IN (SELECT oi.product FROM OrderItem oi)")
    List<Product> findNeverOrderedProducts();

    @Query("SELECT NEW com.formation.products.dto.CategoryStats(p.category.name, COUNT(p), AVG(p.price)) FROM Product p GROUP BY p.category")
    List<CategoryStats> findCategoryStats();

    // --- Part 7: N+1 demo (slow = no fetch, fast = JOIN FETCH already above) ---

    @Query("SELECT p FROM Product p")
    List<Product> findAllSlow();

    @EntityGraph(value = "Product.withCategory")
    @Query("SELECT p FROM Product p")
    List<Product> findAllWithCategoryGraph();

    @EntityGraph(value = "Product.full")
    @Query("SELECT p FROM Product p")
    List<Product> findAllWithFullGraph();
}
