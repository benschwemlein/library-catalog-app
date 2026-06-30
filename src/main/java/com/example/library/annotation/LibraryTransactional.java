package com.example.library.annotation;

import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.*;

/**
 * Meta-annotation for library service write operations. Composes @Transactional
 * with library-wide defaults: READ_COMMITTED isolation, REQUIRED propagation,
 * and rollback on all exceptions (not just RuntimeException).
 *
 * Use @Transactional(readOnly = true) directly for read-only methods.
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Transactional(
    isolation  = Isolation.READ_COMMITTED,
    propagation = Propagation.REQUIRED,
    rollbackFor = Exception.class
)
public @interface LibraryTransactional {
}
