package com.formation.products.repository;

import com.formation.products.dto.CategoryStats;
import com.formation.products.model.Category;
import com.formation.products.model.Product;
import com.formation.products.model.Supplier;
import org.springframework.data.domain.Page;
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

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.supplier WHERE p.category.id = :categoryId ORDER BY p.createdAt DESC")
    List<Product> findByCategoryIdWithCategoryAndSupplier(@Param("categoryId") Long categoryId);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.supplier WHERE p.category = :category ORDER BY p.createdAt DESC")
    List<Product> findByCategoryWithCategoryAndSupplier(@Param("category") Category category);

    @EntityGraph(value = "Product.full")
    Page<Product> findAll(Pageable pageable);

    List<Product> findByCategory(Category category);

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findBySupplier(Supplier supplier);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.supplier WHERE p.price BETWEEN :min AND :max ORDER BY p.price")
    List<Product> findByPriceBetween(@Param("min") BigDecimal min, @Param("max") BigDecimal max);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.supplier WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> searchByName(@Param("keyword") String keyword);

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

    @Query("SELECT p FROM Product p")
    List<Product> findAllSlow();

    @EntityGraph(value = "Product.withCategory")
    @Query("SELECT p FROM Product p")
    List<Product> findAllWithCategoryGraph();

    @EntityGraph(value = "Product.full")
    @Query("SELECT p FROM Product p")
    List<Product> findAllWithFullGraph();

    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.sku = :sku")
    boolean existsBySku(@Param("sku") String sku);

    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.sku = :sku AND p.id <> :id")
    boolean existsBySkuAndIdNot(@Param("sku") String sku, @Param("id") Long id);
}
