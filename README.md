

# 📖 Booking System API

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/SpringBoot-3.x-green)
![Database](https://img.shields.io/badge/Database-MySQL%2FPostgres-orange)
![JWT](https://img.shields.io/badge/Auth-JWT-red)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

A **RESTful booking system** built with **Spring Boot 3, Java 17, and MySQL/PostgreSQL**.
It supports **JWT authentication**, **role-based access (ADMIN/USER)**, and **resource & reservation management** with filtering, pagination, and sorting.

---

## ✨ Features

* 🔐 **Authentication & RBAC**

  * **ADMIN** → Full CRUD on resources & reservations
  * **USER** → View resources, create reservations, view own reservations
* 📦 **Resources**: Manage bookable items (rooms, vehicles, equipment)
* 📝 **Reservations**: Track status (`PENDING`, `CONFIRMED`, `CANCELLED`) with pricing
* 🔍 **Filtering** by status & price + **Pagination & Sorting**
* ⚡ **Secure password storage** (BCrypt)
* 📖 **Swagger/OpenAPI documentation**
* 🛠️ **Postman collection included**

---

## 🛠️ Tech Stack

* **Backend** → Java 17, Spring Boot 3.x
* **Database** → MySQL / PostgreSQL
* **Security** → Spring Security + JWT
* **ORM** → Hibernate / Spring Data JPA
* **Build Tool** → Maven
* **Docs** → Swagger / Springdoc

---

## 🚀 Getting Started

### 1️⃣ Clone the repository

```bash
git clone https://github.com/your-username/booking-system.git
cd booking-system
```

### 2️⃣ Configure database

Update `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/bookingdb
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

For **Postgres**:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/bookingdb
spring.datasource.username=postgres
spring.datasource.password=yourpassword
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

### 3️⃣ Run the app

```bash
mvn spring-boot:run
```

👉 App runs at: `http://localhost:8080`

---

## 👥 Default Users (seeded)

| Role  | Username | Password |
| ----- | -------- | -------- |
| ADMIN | admin    | admin123 |
| USER  | user     | user123  |

---

## 📌 API Endpoints

### 🔑 Authentication

* `POST /api/auth/login` → Login & get JWT
* `POST /api/auth/register` → Register user

### 📦 Resources

* `GET /api/resources` → List all resources (paginated)
* `GET /api/resources/{id}` → Get resource by ID
* `POST /api/resources` → Create (ADMIN only)
* `PUT /api/resources/{id}` → Update (ADMIN only)
* `DELETE /api/resources/{id}` → Delete (ADMIN only)

### 📝 Reservations

* `GET /api/reservations` →

  * ADMIN → all reservations
  * USER → only own reservations
* Supports query params: `status`, `minPrice`, `maxPrice`, `page`, `size`, `sort`
* `GET /api/reservations/{id}` → Reservation by ID
* `POST /api/reservations` → Create reservation
* `PUT /api/reservations/{id}` → Update (ADMIN/owner)
* `DELETE /api/reservations/{id}` → Cancel reservation

---

👉 `http://localhost:8080/swagger-ui.html`

