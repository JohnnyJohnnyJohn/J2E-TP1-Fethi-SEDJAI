package com.formation.products.validation;

import com.formation.products.model.Order;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidDateRangeValidator implements ConstraintValidator<ValidDateRange, Order> {

    @Override
    public boolean isValid(Order order, ConstraintValidatorContext context) {
        if (order == null || order.getOrderDate() == null || order.getDeliveryDate() == null) {
            return true;
        }
        return !order.getDeliveryDate().isBefore(order.getOrderDate());
    }
}
