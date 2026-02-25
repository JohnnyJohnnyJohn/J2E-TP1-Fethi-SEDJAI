package com.formation.products.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

public class ValidPriceValidator implements ConstraintValidator<ValidPrice, BigDecimal> {

    @Override
    public boolean isValid(BigDecimal value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return value.stripTrailingZeros().scale() <= 2;
    }
}
