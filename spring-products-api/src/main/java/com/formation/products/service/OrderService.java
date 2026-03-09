package com.formation.products.service;

import com.formation.products.model.Order;
import com.formation.products.model.OrderItem;
import com.formation.products.model.OrderStatus;
import com.formation.products.model.Product;
import com.formation.products.repository.OrderRepository;
import com.formation.products.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    /**
     * Creates an order from a map of product IDs to quantities.
     * Each product is resolved from the database, transformed into an OrderItem, then
     * the order total is computed before persistence.
     */
    @Transactional
    public Order createOrder(String customerName,
                             String customerEmail,
                             Map<Long, Integer> productsAndQuantities,
                             LocalDateTime orderDate,
                             LocalDateTime deliveryDate) {
        if (productsAndQuantities == null || productsAndQuantities.isEmpty()) {
            throw new IllegalArgumentException("At least one product is required to create an order");
        }

        Order order = new Order();
        order.setCustomerName(customerName != null ? customerName.trim() : null);
        order.setCustomerEmail(customerEmail);
        if (orderDate != null) {
            order.setOrderDate(orderDate);
        }
        if (deliveryDate != null) {
            order.setDeliveryDate(deliveryDate);
        }

        for (Map.Entry<Long, Integer> entry : productsAndQuantities.entrySet()) {
            Long productId = entry.getKey();
            int quantity = entry.getValue() != null ? entry.getValue() : 0;
            if (quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be >= 1 for product " + productId);
            }

            Product product = productRepository.findByIdWithCategoryAndSupplier(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found with id " + productId));

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(quantity);
            item.setUnitPrice(product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO);
            order.addItem(item);
        }

        order.calculateTotal();
        return orderRepository.save(order);
    }

    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id " + orderId));
        order.setStatus(newStatus);
        orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAllWithDetails();
    }

    @Transactional(readOnly = true)
    public Order getOrderById(Long id) {
        return orderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id " + id));
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByCustomerEmail(String email) {
        return orderRepository.findByCustomerEmailWithDetails(email);
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatusWithDetails(status);
    }
}

