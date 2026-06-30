package com.example.library.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuration class that enables JPA auditing for the application.
 * When enabled, JPA automatically populates fields annotated with
 * @CreatedDate, @LastModifiedDate, @CreatedBy, and @LastModifiedBy
 * on entity save/update operations.
 */
@Configuration
@EnableJpaAuditing
public class AuditConfig {
    // JPA auditing is enabled via @EnableJpaAuditing.
    // Entities should use @EntityListeners(AuditingEntityListener.class)
    // along with @CreatedDate and @LastModifiedDate fields of type LocalDateTime.
}
