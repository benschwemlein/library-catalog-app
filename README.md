# Library Catalog Sample App

> **This app intentionally contains bugs and incomplete features. It is designed as a test corpus for LLM-based code navigation and analysis tools, not for production use.**

A library management system built with Spring Boot 3 (Java) and Angular 17 (TypeScript). It covers enough real-world complexity to be a useful benchmark — authentication, role-based access, JPA entity relationships, REST APIs, and a multi-page frontend — while being small enough to inspect results by hand.

## Stack

- **Backend:** Spring Boot 3, Spring Security, JWT, JPA/Hibernate, H2 (in-memory) — 391 Java source files across 21 packages
- **Frontend:** Angular 17, standalone components, RxJS — 143 TypeScript files
- **Tests:** Cucumber-JVM + RestAssured (11 feature files, 33 Java step/support files), Jasmine/Karma (frontend)

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
