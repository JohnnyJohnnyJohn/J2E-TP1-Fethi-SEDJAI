package com.formation.products.repository;

import com.formation.products.model.Product;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Primary
@Profile("jpa")
public class JpaProductRepositoryAdapter implements IProductRepository {

    private final SpringDataProductRepository springDataRepository;

    public JpaProductRepositoryAdapter(SpringDataProductRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Product save(Product product) {
        if (product.getId() == null || product.getId().isEmpty()) {
            product.setId(UUID.randomUUID().toString());
        }
        return springDataRepository.save(product);
    }

    @Override
    public Optional<Product> findById(String id) {
        return springDataRepository.findById(id);
    }

    @Override
    public List<Product> findAll() {
        return springDataRepository.findAll();
    }

    @Override
    public List<Product> findByCategory(String category) {
        return springDataRepository.findByCategoryIgnoreCase(category);
    }

    @Override
    public void delete(String id) {
        springDataRepository.deleteById(id);
    }

    @Override
    public boolean exists(String id) {
        return springDataRepository.existsById(id);
    }

    @Override
    public long count() {
        return springDataRepository.count();
    }
}
