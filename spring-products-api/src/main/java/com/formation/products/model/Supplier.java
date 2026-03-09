package com.formation.products.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "suppliers")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(unique = true)
    private String email;

    @Column(length = 50)
    private String phone;

    @OneToMany(mappedBy = "supplier")
    private List<Product> products = new ArrayList<>();

    public Supplier() {
    }

    public Supplier(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public void addProduct(Product product) {
        Objects.requireNonNull(product, "product must not be null");
        product.setSupplier(this);
    }

    public void removeProduct(Product product) {
        if (product == null) {
            return;
        }
        if (product.getSupplier() == this) {
            product.setSupplier(null);
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @JsonIgnore
    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        for (Product current : new ArrayList<>(this.products)) {
            current.setSupplier(null);
        }
        this.products = new ArrayList<>();
        if (products == null) {
            return;
        }
        for (Product product : products) {
            if (product != null) {
                product.setSupplier(this);
            }
        }
    }
}
