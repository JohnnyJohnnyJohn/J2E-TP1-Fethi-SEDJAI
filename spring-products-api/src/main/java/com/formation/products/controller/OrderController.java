package com.formation.products.controller;

import com.formation.products.model.Order;
import com.formation.products.model.OrderStatus;
import com.formation.products.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
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
            return ResponseEntity.ok(orderService.getByCustomerEmail(customerEmail));
        }
        if (status != null) {
            return ResponseEntity.ok(orderService.getByStatus(status));
        }
        return ResponseEntity.ok(orderService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(orderService.getById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequest request) {
        try {
            Order created = orderService.createOrder(
                    request.getCustomerName(),
                    request.getCustomerEmail(),
                    request.getProductsAndQuantities());
            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(created.getId())
                    .toUri();
            return ResponseEntity.created(location).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id,
                                          @RequestBody UpdateStatusRequest request) {
        try {
            orderService.updateOrderStatus(id, request.getStatus());
            return ResponseEntity.ok(orderService.getById(id));
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().contains("not found")) {
                return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
            }
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    public static class CreateOrderRequest {
        private String customerName;
        private String customerEmail;
        private Map<Long, Integer> productsAndQuantities;

        public String getCustomerName() {
            return customerName;
        }

        public void setCustomerName(String customerName) {
            this.customerName = customerName;
        }

        public String getCustomerEmail() {
            return customerEmail;
        }

        public void setCustomerEmail(String customerEmail) {
            this.customerEmail = customerEmail;
        }

        public Map<Long, Integer> getProductsAndQuantities() {
            return productsAndQuantities;
        }

        public void setProductsAndQuantities(Map<Long, Integer> productsAndQuantities) {
            this.productsAndQuantities = productsAndQuantities;
        }
    }

    public static class UpdateStatusRequest {
        private OrderStatus status;

        public OrderStatus getStatus() {
            return status;
        }

        public void setStatus(OrderStatus status) {
            this.status = status;
        }
    }
}

