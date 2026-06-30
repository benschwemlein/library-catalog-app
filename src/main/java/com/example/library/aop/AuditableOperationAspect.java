package com.example.library.aop;

import com.example.library.annotation.AuditableOperation;
import com.example.library.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Intercepts methods annotated with @AuditableOperation and writes an AuditLog
 * entry via AuditService after the method completes successfully.
 *
 * The entityId is extracted from the first Long argument of the method, if present.
 * The acting userId is resolved from the Spring Security context.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditableOperationAspect {

    private final AuditService auditService;

    @Around("@annotation(auditableOperation)")
    public Object audit(ProceedingJoinPoint joinPoint, AuditableOperation auditableOperation) throws Throwable {
        Object result = joinPoint.proceed();

        try {
            Long entityId = extractFirstLongArg(joinPoint.getArgs());
            Long userId   = resolveUserId();

            auditService.logAction(
                auditableOperation.action(),
                auditableOperation.entityType(),
                entityId,
                userId,
                null,
                null,
                null
            );
        } catch (Exception e) {
            log.warn("Failed to write audit log for action={} entityType={}: {}",
                auditableOperation.action(), auditableOperation.entityType(), e.getMessage());
        }

        return result;
    }

    private Long extractFirstLongArg(Object[] args) {
        if (args == null) return null;
        for (Object arg : args) {
            if (arg instanceof Long) return (Long) arg;
        }
        return null;
    }

    private Long resolveUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) return null;
            Object principal = auth.getPrincipal();
            if (principal instanceof com.example.library.entity.Member m) return m.getId();
        } catch (Exception ignored) {}
        return null;
    }
}
