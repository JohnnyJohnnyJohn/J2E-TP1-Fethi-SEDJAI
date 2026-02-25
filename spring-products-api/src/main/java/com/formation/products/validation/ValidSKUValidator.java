package com.formation.products.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidSKUValidator implements ConstraintValidator<ValidSKU, String> {

    private static final String SKU_PATTERN = "^[A-Z]{3}\\d{3}$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return value.matches(SKU_PATTERN);
    }
}
