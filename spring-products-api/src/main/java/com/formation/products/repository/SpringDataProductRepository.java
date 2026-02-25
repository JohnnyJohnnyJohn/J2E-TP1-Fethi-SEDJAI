package com.formation.products.repository;

import com.formation.products.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataProductRepository extends JpaRepository<Product, String> {
    
    List<Product> findByCategoryIgnoreCase(String category);
}
