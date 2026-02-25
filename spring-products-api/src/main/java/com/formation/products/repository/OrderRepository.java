package com.formation.products.repository;

import com.formation.products.dto.MostOrderedProduct;
import com.formation.products.dto.OrderStatusCount;
import com.formation.products.model.Order;
import com.formation.products.model.OrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomerEmail(String customerEmail);

    List<Order> findByStatus(OrderStatus status);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.product")
    List<Order> findOrdersWithItems();

    @EntityGraph(attributePaths = {"items", "items.product"})
    List<Order> findAll();

    // --- Part 6: Aggregation ---

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = :status")
    BigDecimal getTotalRevenueByStatus(@Param("status") OrderStatus status);

    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> countByStatusRaw();

    @Query("SELECT NEW com.formation.products.dto.OrderStatusCount(o.status, COUNT(o)) FROM Order o GROUP BY o.status")
    List<OrderStatusCount> countByStatus();

    @Query("SELECT NEW com.formation.products.dto.MostOrderedProduct(oi.product.id, oi.product.name, SUM(oi.quantity), SUM(oi.subtotal)) FROM OrderItem oi GROUP BY oi.product.id, oi.product.name ORDER BY SUM(oi.quantity) DESC")
    List<MostOrderedProduct> findMostOrderedProducts(Pageable pageable);
}

