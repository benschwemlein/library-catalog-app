package com.example.library.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validates that the annotated String is a well-formed ISBN-10 or ISBN-13,
 * including check-digit verification. Null and blank values are considered
 * valid; combine with @NotBlank to reject them.
 */
@Documented
@Constraint(validatedBy = IsbnConstraintValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidIsbn {
    String message() default "Invalid ISBN — must be a valid ISBN-10 or ISBN-13 including check digit";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
