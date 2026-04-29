# Flight Booking Service

A production-grade, multi-tenant flight booking and management backend system. Designed to support multiple airlines (tenants) operating on the same physical instance with strict logical data isolation.

## 🚀 Key Features

*   **Multi-Tenancy**: Data isolation using discriminator columns (`tenant_id`). Tenant context is securely resolved and propagated via Spring Security's `SecurityContext` and a thread-local `TenantContext`.
*   **Flight & Inventory Management**: Create and manage flights. Seat reservations are protected against concurrent bookings using **Optimistic Locking** (`@Version`).
*   **Booking Engine**: Complete PNR (Passenger Name Record) generation. Bookings expire automatically via a scheduled TTL job if not paid.
*   **Ticketing**: Automatic issuance of unique tickets for each passenger upon booking confirmation.
*   **Payments Integration**: Extensible payment provider abstraction with support for Provider Webhooks (idempotent), Cash, and Bank Transfer workflows.
*   **Tenant Configurations (EAV)**: Entity-Attribute-Value pattern for tenant-specific settings.
*   **Advanced Security**: 
    *   Stateless HTTP Basic Authentication.
    *   Configurable **Password History Policy** per tenant (prevents reuse of the last *N* passwords).
*   **Caching**: High-performance flight search caching using Redis (`@Cacheable`, `@CacheEvict`).
*   **Database Migrations**: Version-controlled schema management using Liquibase.

## 🛠️ Technology Stack

*   **Java 21**
*   **Spring Boot 4.0.6**
    *   Spring Web (REST APIs)
    *   Spring Data JPA (Hibernate 6)
    *   Spring Security
    *   Spring Cache
*   **PostgreSQL**: Primary relational database.
*   **Redis**: Distributed caching layer.
*   **Liquibase**: Database schema versioning and migrations.
*   **MapStruct**: High-performance, type-safe DTO mapping.
*   **Lombok**: Boilerplate code reduction.
*   **JUnit 5 & Mockito**: Unit and Integration testing.
*   **SpringDoc OpenAPI (Swagger)**: Interactive API documentation.

## 📋 Prerequisites

Before running the application, ensure you have the following installed:

*   **JDK 21** or later
*   **PostgreSQL 15+** (or use Docker)
*   **Redis** (or use Docker)
*   **Gradle** (Optional, project includes Gradle Wrapper)

## ⚙️ Getting Started

### 1. Environment Setup

Copy the example environment file and configure your local database and Redis credentials:

```bash
cp .env.example .env.local
```

Update `.env.local` with your local PostgreSQL and Redis connection details.
*(Note: `.env.local` is git-ignored and should be used for your local overrides).*

### 2. Database & Redis Preparation

**Mac (via Homebrew):**
```bash
brew install postgresql redis
brew services start postgresql
brew services start redis
```

**Windows / General (via Docker):**
```bash
docker run -d -p 5432:5432 -e POSTGRES_PASSWORD=postgres postgres:15
docker run -d -p 6379:6379 redis
```

Once running, create the PostgreSQL database:
```sql
CREATE DATABASE flight_booking_db;
```
*Liquibase will automatically create all tables, indexes, and foreign keys on startup.*

### 3. Build the Application

Use the Gradle wrapper to build the application and download dependencies:

```bash
./gradlew clean build
```

*(Note: To run the build without running tests, use `./gradlew clean build -x test`)*

### 4. Run the Application

You can run the application directly using Gradle:

```bash
./gradlew bootRun
```

Or run the compiled JAR file:

```bash
java -jar build/libs/flight-booking-service-0.0.1-SNAPSHOT.jar
```

**Data Initialization:** On the first startup, the application will read `app.data-initializer.*` properties from your `application.yml` and automatically bootstrap a default Tenant and an initial `ADMIN` user. Check the console logs for the credentials.

## 🧪 Testing

The project uses JUnit 5 and Mockito.

To run the unit test suite:

```bash
./gradlew test
```

## 📚 API Documentation

The API is fully documented using OpenAPI 3. Once the application is running (default port `8080`), you can access the Swagger UI to explore and test the endpoints interactively:

*   **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
*   **OpenAPI JSON**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

To authenticate in Swagger UI, click the **Authorize** button and enter the credentials of the bootstrapped Admin user.

## 🏗️ Architecture Overview

The project follows a clean, layered architecture:

*   `controller`: REST API endpoints, DTO validation.
*   `service`: Core business logic, transaction boundaries (`@Transactional`), caching rules.
*   `repository`: Spring Data JPA interfaces and dynamic `Specification` builders.
*   `domain`: JPA Entities, Enums, and rich domain methods (preventing anemic models).
*   `dto`: Request/Response data transfer objects.
*   `mapper`: MapStruct interfaces for Entity-DTO conversion.
*   `security`: Authentication filters, UserDetails wrappers, and the `TenantContext` lifecycle.
*   `integration`: Interfaces and stubs for external systems (e.g., Payment Gateways).
*   `exception`: Custom domain exceptions and the `@RestControllerAdvice` global handler.
*   `config`: Spring configuration classes (Redis, OpenAPI, Security, Schedulers).
