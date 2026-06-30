# Library Catalog Sample App

> **This app intentionally contains bugs and incomplete features. It is designed as a test corpus for LLM-based code navigation and analysis tools, not for production use.**

A library management system built with Spring Boot 3 (Java) and Angular 17 (TypeScript). It covers enough real-world complexity to be a useful benchmark — authentication, role-based access, JPA entity relationships, REST APIs, and a multi-page frontend — while being small enough to inspect results by hand.

## Stack

- **Backend:** Spring Boot 3, Spring Security, JWT, JPA/Hibernate, H2 (in-memory) — 397 Java source files across 21 packages
- **Frontend:** Angular 17, standalone components, RxJS — 143 TypeScript files
- **Tests:** Cucumber-JVM + RestAssured (11 feature files, 33 Java step/support files), Jasmine/Karma (frontend)

## Custom annotations

Three custom annotations in `com.example.library.annotation`, each representing a distinct Java annotation use case:

| Annotation | Type | What it does |
|---|---|---|
| `@ValidIsbn` | Bean Validation constraint | Validates ISBN-10 and ISBN-13 check digits via `IsbnConstraintValidator`. Applied to `CreateBookRequest.isbn`. |
| `@AuditableOperation` | AOP-backed marker | `AuditableOperationAspect` intercepts annotated methods and writes an `AuditLog` entry via `AuditService`. Applied to checkout, return, renew, pay fine, and waive fine. |
| `@LibraryTransactional` | Meta-annotation | Composes `@Transactional` with library-wide defaults: `READ_COMMITTED` isolation, `REQUIRED` propagation, rollback on all exceptions. |

## Design patterns

The backend implements nine GoF patterns across the `com.example.library.pattern` package, making it a useful corpus for code-navigation queries about pattern usage:

| Pattern | Where used |
|---|---|
| **Builder** | `BookSearchCriteria`, `LoanQueryCriteria`, `MemberSearchCriteria`, `ReportCriteria` |
| **Chain of Responsibility** | `LoanEligibilityChain` — six handlers validate checkout eligibility in sequence |
| **Command** | `LibraryCommandService` — checkout, pay fine, place hold, renew, transfer copy |
| **Factory** | `BookCopyFactory`, `MemberFactory`, `NotificationFactory`, `ReportFactory` |
| **Observer** | `NotificationEventListener`, `AuditEventListener`, `StatisticsEventListener` listen for Spring application events |
| **Specification** | `IsEligibleToBorrowSpecification`, `IsOverdueSpecification`, `IsPremiumMemberSpecification`, and 13 others |
| **State** | `LoanStateMachine` and `HoldStateMachine` — enforce valid lifecycle transitions |
| **Strategy** | `OverdueFineContext` selects `StandardFineStrategy`, `PremiumFineStrategy`, or `StudentFineStrategy` by membership tier |
| **Template Method** | `AbstractReportGenerator` — five concrete report generators override hook methods |

## Enterprise patterns

Five patterns common in production Spring Boot applications, each added as a realistic example in the corpus:

| Pattern | What was added |
|---|---|
| **@ConfigurationProperties** | `LibraryProperties` binds the `application.library.*` namespace (max loans, fines threshold, renewal limits, etc.) and replaces hardcoded constants in `LoanService`. |
| **Spring Cache (`@Cacheable`/`@CacheEvict`)** | `CacheConfig` declares six named caches; `BookService.findById` and `findByIsbn` are cached, evicted on update/delete. A `recommendations` cache supports `RecommendationCache`. |
| **Optimistic locking (`@Version`)** | `@Version Long version` added to `Book`, `BookCopy`, `Loan`, `Hold`, and `Member`. `GlobalExceptionHandler` returns HTTP 409 on `ObjectOptimisticLockingFailureException`. |
| **Soft deletes** | `Book`, `BookCopy`, and `Member` use Hibernate 6 `@SQLDelete` + `@SQLRestriction("deleted = false")`. Deleting any of these sets `deleted = true` and `deleted_at` instead of removing the row. |
| **Flyway migrations** | `src/main/resources/db/migration/` contains three versioned SQL scripts (`V1__core_schema.sql`, `V2__circulation_schema.sql`, `V3__features_schema.sql`). Flyway is disabled in dev (H2 uses `create-drop`) and enabled via `application-prod.yml`. |

## Users (seeded on startup)

| Email | Password | Role |
|---|---|---|
| alice@citylibrary.org | password123 | ADMIN |
| bob@citylibrary.org | password123 | MANAGER |
| carol@example.com | password123 | USER |

## Running

```bash
# Backend (port 8080)
./gradlew bootRun

# Frontend (port 4200)
cd catalog-ui && npm install && ng serve
```

## Known issues

The app has intentional and unintentional bugs, incomplete implementations, and areas of technical debt. It is meant to give an LLM-based tool something realistic to reason about — not a clean starting point for a real project.
