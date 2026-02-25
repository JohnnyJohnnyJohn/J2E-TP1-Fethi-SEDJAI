package com.formation.products.repository;

import com.formation.products.model.Product;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@ApplicationScoped
@InMemory
public class InMemoryProductRepository implements IProductRepository {

    private final ConcurrentHashMap<String, Product> products = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        Product laptop = new Product("MacBook Pro 14\"", "Apple M3 Pro, 18GB RAM, 512GB SSD", 
                new BigDecimal("2499.99"), "Electronics", 15);
        products.put(laptop.getId(), laptop);

        Product phone = new Product("iPhone 15 Pro", "256GB, Titanium Blue", 
                new BigDecimal("1199.99"), "Electronics", 25);
        products.put(phone.getId(), phone);

        Product book = new Product("Clean Code", "A Handbook of Agile Software Craftsmanship by Robert C. Martin", 
                new BigDecimal("44.99"), "Books", 100);
        products.put(book.getId(), book);

        Product headphones = new Product("Sony WH-1000XM5", "Wireless Noise Cancelling Headphones", 
                new BigDecimal("349.99"), "Electronics", 30);
        products.put(headphones.getId(), headphones);

        Product coffee = new Product("Ethiopian Yirgacheffe", "Premium Single Origin Coffee Beans 1kg", 
                new BigDecimal("29.99"), "Food", 50);
        products.put(coffee.getId(), coffee);
    }

    @Override
    public Product save(Product product) {
        if (product.getId() == null || product.getId().isEmpty()) {
            product.setId(UUID.randomUUID().toString());
        }
        products.put(product.getId(), product);
        return product;
    }

    @Override
    public Optional<Product> findById(String id) {
        return Optional.ofNullable(products.get(id));
    }

    @Override
    public List<Product> findAll() {
        return new ArrayList<>(products.values());
    }

    @Override
    public List<Product> findByCategory(String category) {
        return products.values().stream()
                .filter(p -> p.getCategory() != null && p.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    @Override
    public void delete(String id) {
        products.remove(id);
    }

    @Override
    public boolean exists(String id) {
        return products.containsKey(id);
    }

    @Override
    public long count() {
        return products.size();
    }
}
