package com.formation.products.dto;

import com.formation.products.model.OrderStatus;

/**
 * DTO for JPQL projection: order status and count.
 */
public class OrderStatusCount {

    private OrderStatus status;
    private Long count;

    public OrderStatusCount(OrderStatus status, Long count) {
        this.status = status;
        this.count = count;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Long getCount() {
        return count;
    }
}
