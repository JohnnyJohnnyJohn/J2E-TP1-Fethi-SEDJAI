package com.formation.products.service;

import com.formation.products.dto.CategoryStats;
import com.formation.products.dto.MostOrderedProduct;
import com.formation.products.dto.OrderStatusCount;
import com.formation.products.model.Category;
import com.formation.products.model.OrderStatus;
import com.formation.products.model.Product;
import com.formation.products.repository.CategoryRepository;
import com.formation.products.repository.OrderRepository;
import com.formation.products.repository.ProductRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class StatsService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final CategoryRepository categoryRepository;

    public StatsService(ProductRepository productRepository,
                        OrderRepository orderRepository,
                        CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<Object[]> getCountByCategory() {
        return productRepository.countByCategory();
    }

    @Transactional(readOnly = true)
    public List<Object[]> getAveragePriceByCategory() {
        return productRepository.averagePriceByCategory();
    }

    @Transactional(readOnly = true)
    public List<Product> getTopExpensive(int limit) {
        return productRepository.findTopExpensive(PageRequest.of(0, limit));
    }

    @Transactional(readOnly = true)
    public List<Product> getNeverOrderedProducts() {
        return productRepository.findNeverOrderedProducts();
    }

    @Transactional(readOnly = true)
    public List<CategoryStats> getCategoryStats() {
        return productRepository.findCategoryStats();
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalRevenue() {
        BigDecimal sum = orderRepository.getTotalRevenueByStatus(OrderStatus.DELIVERED);
        return sum != null ? sum : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public List<OrderStatusCount> getCountByStatus() {
        return orderRepository.countByStatus();
    }

    @Transactional(readOnly = true)
    public List<MostOrderedProduct> getMostOrderedProducts(int limit) {
        return orderRepository.findMostOrderedProducts(PageRequest.of(0, limit));
    }

    @Transactional(readOnly = true)
    public List<Category> getCategoriesWithMinProducts(int minProducts) {
        return categoryRepository.findCategoriesWithMinProducts(minProducts);
    }
}
