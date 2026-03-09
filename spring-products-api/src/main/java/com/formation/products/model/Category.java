package com.formation.products.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.BatchSize;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "categories")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom de la catégorie est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre {min} et {max} caractères")
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Size(max = 500, message = "La description ne peut pas dépasser {max} caractères")
    @Column(length = 500)
    private String description;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    @BatchSize(size = 10)
    private List<Product> products = new ArrayList<>();

    public Category() {
    }

    public Category(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public void addProduct(Product product) {
        Objects.requireNonNull(product, "product must not be null");
        product.setCategory(this);
    }

    public void removeProduct(Product product) {
        if (product == null) {
            return;
        }
        if (product.getCategory() == this) {
            product.setCategory(null);
        } else {
            products.remove(product);
        }
    }

    void addProductInternal(Product product) {
        if (!products.contains(product)) {
            products.add(product);
        }
    }

    void removeProductInternal(Product product) {
        products.remove(product);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonIgnore
    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        for (Product current : new ArrayList<>(this.products)) {
            current.setCategory(null);
        }
        this.products = new ArrayList<>();
        if (products == null) {
            return;
        }
        for (Product product : products) {
            if (product != null) {
                product.setCategory(this);
            }
        }
    }
}
