package com.example.library.annotation;

import java.lang.annotation.*;

/**
 * Marks a method as an auditable library operation. AuditableOperationAspect
 * intercepts annotated methods and writes an AuditLog entry via AuditService.
 *
 * The first Long parameter of the annotated method is used as the entityId.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditableOperation {

    /** Short label for the operation, e.g. "CHECKOUT", "RETURN", "PAY_FINE". */
    String action();

    /** The domain entity type being acted on, e.g. "Loan", "Fine", "Hold". */
    String entityType() default "";
}
