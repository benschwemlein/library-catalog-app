package com.example.library.annotation;

import com.example.library.util.IsbnValidator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class IsbnConstraintValidator implements ConstraintValidator<ValidIsbn, String> {

    @Autowired
    private IsbnValidator isbnValidator;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return isbnValidator.validateIsbn13(value) || isbnValidator.validateIsbn10(value);
    }
}
