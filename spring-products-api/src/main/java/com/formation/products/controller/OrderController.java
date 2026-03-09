package com.formation.products.controller;

import com.formation.products.model.Order;
import com.formation.products.model.OrderStatus;
import com.formation.products.service.OrderService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Order creation and status management")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders(
            @RequestParam(required = false) String customerEmail,
            @RequestParam(required = false) OrderStatus status) {
        if (customerEmail != null) {
            return ResponseEntity.ok(orderService.getOrdersByCustomerEmail(customerEmail));
        }
        if (status != null) {
            return ResponseEntity.ok(orderService.getOrdersByStatus(status));
        }
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Order created = orderService.createOrder(
                request.getCustomerName(),
                request.getCustomerEmail(),
                request.getProductsAndQuantities(),
                request.getOrderDate(),
                request.getDeliveryDate());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Order> updateStatus(@PathVariable Long id,
                                          @RequestBody UpdateStatusRequest request) {
        orderService.updateOrderStatus(id, request.getStatus());
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    public static class CreateOrderRequest {
        @NotBlank(message = "Le nom du client est obligatoire")
        @Size(min = 2, max = 100, message = "Le nom du client doit contenir entre {min} et {max} caractères")
        private String customerName;

        @Email(message = "L'adresse email n'est pas valide")
        private String customerEmail;

        @NotEmpty(message = "Au moins un produit est requis")
        private Map<Long, Integer> productsAndQuantities;

        private LocalDateTime orderDate;
        private LocalDateTime deliveryDate;

        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public String getCustomerEmail() { return customerEmail; }
        public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
        public Map<Long, Integer> getProductsAndQuantities() { return productsAndQuantities; }
        public void setProductsAndQuantities(Map<Long, Integer> productsAndQuantities) { this.productsAndQuantities = productsAndQuantities; }
        public LocalDateTime getOrderDate() { return orderDate; }
        public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
        public LocalDateTime getDeliveryDate() { return deliveryDate; }
        public void setDeliveryDate(LocalDateTime deliveryDate) { this.deliveryDate = deliveryDate; }
    }

    public static class UpdateStatusRequest {
        private OrderStatus status;
        public OrderStatus getStatus() { return status; }
        public void setStatus(OrderStatus status) { this.status = status; }
    }
}
