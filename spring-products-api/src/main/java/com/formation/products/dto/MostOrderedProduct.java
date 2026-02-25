package com.formation.products.dto;

import java.math.BigDecimal;

/**
 * DTO for JPQL projection: product id, name, total quantity ordered.
 */
public class MostOrderedProduct {

    private Long productId;
    private String productName;
    private Long totalQuantity;
    private BigDecimal totalRevenue;

    public MostOrderedProduct(Long productId, String productName, Long totalQuantity, BigDecimal totalRevenue) {
        this.productId = productId;
        this.productName = productName;
        this.totalQuantity = totalQuantity;
        this.totalRevenue = totalRevenue;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public Long getTotalQuantity() {
        return totalQuantity;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }
}
