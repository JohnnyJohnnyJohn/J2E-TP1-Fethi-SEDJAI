package com.formation.products.dto;

import java.math.BigDecimal;

/**
 * DTO for JPQL projection: category name, product count, average price.
 * Constructor used by: SELECT NEW ... CategoryStats(p.category.name, COUNT(p), AVG(p.price))
 */
public class CategoryStats {

    private String categoryName;
    private Long productCount;
    private BigDecimal averagePrice;

    public CategoryStats(String categoryName, Long productCount, Double averagePrice) {
        this.categoryName = categoryName;
        this.productCount = productCount;
        this.averagePrice = averagePrice != null ? BigDecimal.valueOf(averagePrice) : null;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public Long getProductCount() {
        return productCount;
    }

    public BigDecimal getAveragePrice() {
        return averagePrice;
    }
}
