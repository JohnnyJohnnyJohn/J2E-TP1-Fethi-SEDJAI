package com.formation.products.repository;

import com.formation.products.model.Product;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
@Transactional
@JpaRepository
public class JpaProductRepository implements IProductRepository {

    @PersistenceContext(unitName = "ProductsPU")
    private EntityManager entityManager;

    @Override
    public Product save(Product product) {
        if (product.getId() == null || product.getId().isEmpty()) {
            product.setId(UUID.randomUUID().toString());
        }
        
        if (entityManager.find(Product.class, product.getId()) != null) {
            return entityManager.merge(product);
        } else {
            entityManager.persist(product);
            return product;
        }
    }

    @Override
    public Optional<Product> findById(String id) {
        Product product = entityManager.find(Product.class, id);
        return Optional.ofNullable(product);
    }

    @Override
    public List<Product> findAll() {
        return entityManager.createQuery("SELECT p FROM Product p ORDER BY p.createdAt DESC", Product.class)
                .getResultList();
    }

    @Override
    public List<Product> findByCategory(String category) {
        return entityManager.createQuery(
                "SELECT p FROM Product p WHERE LOWER(p.category) = LOWER(:category) ORDER BY p.name", 
                Product.class)
                .setParameter("category", category)
                .getResultList();
    }

    @Override
    public void delete(String id) {
        Product product = entityManager.find(Product.class, id);
        if (product != null) {
            entityManager.remove(product);
        }
    }

    @Override
    public boolean exists(String id) {
        return entityManager.find(Product.class, id) != null;
    }

    @Override
    public long count() {
        return entityManager.createQuery("SELECT COUNT(p) FROM Product p", Long.class)
                .getSingleResult();
    }
}
