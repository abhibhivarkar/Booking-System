# Booking System (Spring Boot + JWT + RBAC)

A simple RESTful booking system built with **Spring Boot 3.x (Java 17+)** and **MySQL / PostgreSQL**.

It offers two main entities:

* **Resource** — a bookable item (room, vehicle, equipment)
* **Reservation** — a booking for a resource (status: `PENDING`, `CONFIRMED`, `CANCELLED`) with price and time range

Security: Stateless JWT authentication + Role-Based Access Control (ADMIN / USER). Passwords are stored using BCrypt.

---

## Tech stack

* Java 17+
* Spring Boot 3.x
* Spring Security (JWT)
* Spring Data JPA (MySQL / PostgreSQL)
* (Optional) Lombok
* (Optional) Springdoc OpenAPI / Swagger UI
* Maven or Gradle

---

## Features

* Authentication: `POST /auth/login` -> returns JWT access token
* Resources (ADMIN: full CRUD; USER: read-only)

  * `GET /resources` (paginated)
  * `GET /resources/{id}`
  * `POST /resources` (ADMIN)
  * `PUT /resources/{id}` (ADMIN)
  * `DELETE /resources/{id}` (ADMIN)
* Reservations

  * `GET /reservations` (ADMIN: all, USER: own only). Query params: `status`, `minPrice`, `maxPrice`, `page`, `size`, `sort` (e.g. `createdAt,desc`)
  * `GET /reservations/{id}` (ADMIN: any, USER: owned only)
  * `POST /reservations` (USER or ADMIN). For USER role, `userId` is derived from JWT principal.
  * `PUT /reservations/{id}`
  * `DELETE /reservations/{id}`
* Optional: Prevent overlapping CONFIRMED reservations for the same resource & time range
* Pagination, filtering, and optional sorting for reservations

---

## Repository contents (suggested)

```
booking-system/
├── src/main/java/.../config/        # Security, JWT, WebConfig
├── src/main/java/.../controller/    # REST controllers
├── src/main/java/.../dto/           # Request/response DTOs
├── src/main/java/.../model/         # JPA entities (User, Role, Resource, Reservation, enums)
├── src/main/java/.../repository/    # Spring Data JPA repositories
├── src/main/java/.../service/       # Business logic
├── src/main/java/.../security/      # JWT utils, filters
├── src/main/resources/
│   ├── application.yml (or .properties)
│   └── data.sql (optional seed)
└── pom.xml
```

---

## Getting started (local)

These instructions assume **Maven** and **MySQL**. Replace DB details with PostgreSQL if desired.

### 1) Clone repository

```bash
git clone https://github.com/<your-username>/booking-system.git
cd booking-system
```

### 2) Environment variables / application properties

Create `src/main/resources/application.yml` or set environment variables. Example `application.yml` using MySQL:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/booking_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: booking_user
    password: booking_pass
  jpa:
    hibernate:
      ddl-auto: update   # use `validate` or migrations in production
    properties:
      hibernate:
        format_sql: true

app:
  jwt:
    secret: VERY_SECRET_KEY_CHANGE_ME
    expiration-ms: 3600000   # 1 hour

server:
  port: 8080
```

> **Important:** Replace `app.jwt.secret` with a strong secret in production and keep it in environment variables or a secrets manager.

Recommended environment variables (example):

```bash
export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/booking_db
export SPRING_DATASOURCE_USERNAME=booking_user
export SPRING_DATASOURCE_PASSWORD=booking_pass
export APP_JWT_SECRET="your_super_secret_here"
```

### 3) Database

Create the database and user (example MySQL):

```sql
CREATE DATABASE booking_db;
CREATE USER 'booking_user'@'%' IDENTIFIED BY 'booking_pass';
GRANT ALL PRIVILEGES ON booking_db.* TO 'booking_user'@'%';
FLUSH PRIVILEGES;
```

Alternatively use Docker Compose (recommended for local dev) — include a `docker-compose.yml` in repo.

### 4) Seed users (development)

Two convenient options:

**A) `data.sql` with pre-hashed BCrypt passwords**

* Pre-generate BCrypt hashes for `admin123` and `user123` and insert rows for users and roles.

**B) CommandLineRunner bean that creates seed users on startup** (recommended for dev):

```java
@Bean
CommandLineRunner seed(UserService userService) {
  return args -> {
    if (!userService.existsByUsername("admin")) {
      userService.createAdmin("admin", "admin123");
    }
    if (!userService.existsByUsername("user")) {
      userService.createUser("user", "user123");
    }
  };
}
```

**Default test credentials (documented):**

* Admin: `admin` / `admin123` (ROLE\_ADMIN)
* User: `user` / `user123` (ROLE\_USER)

> NOTE: On first startup the passwords will be BCrypt-hashed and saved to DB. Change them for production.

### 5) Build & run

With Maven:

```bash
mvn clean package
java -jar target/booking-system-0.0.1-SNAPSHOT.jar
```

Or from IDE (Run `BookingSystemApplication` main class).

### 6) Swagger / OpenAPI

If Springdoc OpenAPI is enabled, Swagger UI will be available at:

```
http://localhost:8080/swagger-ui.html
# or
http://localhost:8080/swagger-ui/index.html
```

---

## Authentication flow

1. `POST /auth/login` with JSON:

```json
{ "username": "admin", "password": "admin123" }
```

2. Response contains `accessToken`.
3. Use the token in `Authorization` header for subsequent requests:

```
Authorization: Bearer <token>
```

Example curl:

```bash
curl -X POST "http://localhost:8080/auth/login" -H "Content-Type: application/json" -d '{"username":"user","password":"user123"}'
```

---

## Key API endpoints (summary)

### Resources

* `GET /resources` (paginated) — params: `page`, `size`, `sort`
* `GET /resources/{id}`
* `POST /resources` (ADMIN)
* `PUT /resources/{id}` (ADMIN)
* `DELETE /resources/{id}` (ADMIN)

### Reservations

* `GET /reservations` — query params: `status`, `minPrice`, `maxPrice`, `page`, `size`, `sort` (e.g., `sort=createdAt,desc`)

  * ADMIN: returns all reservations (paginated)
  * USER: returns only reservations owned by the authenticated user
* `GET /reservations/{id}` — ADMIN any; USER only if owner
* `POST /reservations` — create reservation. For USER the `userId` should not be passed; backend derives user from JWT.
* `PUT /reservations/{id}` — update reservation
* `DELETE /reservations/{id}`

### Example reservation filter request

```
GET /reservations?status=CONFIRMED&minPrice=10.0&maxPrice=200.0&page=0&size=20&sort=createdAt,desc
```

---

## Filtering, Pagination & Sorting details

* Status must be one of `PENDING`, `CONFIRMED`, `CANCELLED` (case-insensitive handling recommended)
* Price filters are inclusive bounds (`>= minPrice`, `<= maxPrice`)
* Use `Pageable` in Spring Data JPA controllers and repositories; implement dynamic query with `Specification` or `QueryDSL` or `CriteriaBuilder` to combine filters.

---

## Prevent overlapping CONFIRMED reservations (optional)

Suggested approach:

* When creating/updating a reservation with status `CONFIRMED`, check for existing `CONFIRMED` reservations for the same resource where times overlap:

  * overlap condition: `existing.startTime < newEnd && existing.endTime > newStart`
* If overlapping found, reject request with `409 CONFLICT` and meaningful message.

---

## Tests (recommended)

* Unit tests for services and security (mock JWT & Authentication)
* Integration / slice tests using `@DataJpaTest` and `@SpringBootTest` with embedded DB (H2) or Testcontainers for MySQL/Postgres

Example maven command:

```bash
mvn test
```

---

## Postman collection / OpenAPI

* Include a Postman collection in repo (`postman/BookingSystem.postman_collection.json`) with pre-configured requests:

  * `/auth/login`
  * Resource CRUD
  * Reservation CRUD
  * Examples of filter/pagination
* If using Springdoc OpenAPI, include the generated OpenAPI YAML in `docs/openapi.yml` too.

---

## Assumptions & trade-offs

* `app.jwt.secret` is a symmetric secret for JWT signing (HMAC). For higher security use asymmetric keys.
* `ddl-auto: update` in dev only — production should use migrations (Flyway/Liquibase) and `validate`.
* By design, `USER` role cannot create reservations for other users. The backend derives the `user` from JWT.
* Reservation price is a decimal (mapped to `BigDecimal` in Java). Currency handling is out-of-scope.
* Overlap checking for CONFIRMED reservations is optional and may cause performance impact for large datasets; use DB indexes and efficient queries.

---

## Troubleshooting

* `Bad credentials` at login: verify user exists and password matches (BCrypt). If using seeded SQL, ensure password is hashed.
* `No enum constant` errors: ensure enum values used in requests exactly match enum names or implement case-insensitive mapping.
* 401 / 403 errors: verify `Authorization` header format `Bearer <token>` and that token hasn't expired.

---

## Contributing

1. Fork repository
2. Create a feature branch
3. Commit changes with clear messages
4. Open a pull request

---

## License

MIT

---

If you want, I can also:

* generate a ready `application.yml`, `data.sql`, and `docker-compose.yml` for local dev
* create sample Postman collection JSON
* provide full code skeleton (entities, DTOs, controllers, services, security config)

Tell me which of the above you'd like next and I will add it to the repo README or create files for you.
