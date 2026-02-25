package com.formation.products.repository;

import com.formation.products.model.Product;

import java.util.List;
import java.util.Optional;

public interface IProductRepository {

    Product save(Product product);

    Optional<Product> findById(String id);

    List<Product> findAll();

    List<Product> findByCategory(String category);

    void delete(String id);

    boolean exists(String id);

    long count();
}
