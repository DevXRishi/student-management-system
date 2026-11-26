# EduCore – Secure Student Administration Platform

A RESTful backend for managing student records, built with **Spring Boot** using a layered architecture. The APIs are secured with **JWT authentication** and **role-based authorization** using Spring Security.

**EduCore** is the project name. The repository is named `student-management-system`.

## Features

- CRUD operations for student records
- Request and response DTOs with input validation
- Pagination and sorting on list endpoints
- JWT-based authentication and role-based authorization (Spring Security)
- Centralized exception handling with consistent error responses
- Interactive API documentation with Swagger/OpenAPI
- Unit and API tests with JUnit 5, Mockito and MockMvc (16 passing tests)

## Tech Stack

| Area | Technologies |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot, Spring MVC |
| Security | Spring Security, JWT |
| Persistence | Spring Data JPA, Hibernate, MySQL |
| Testing | JUnit 5, Mockito, MockMvc |
| API Docs | Swagger / OpenAPI |
| Build Tool | Maven |

## Architecture

The application follows a layered architecture, so each layer has a single responsibility.

```
Client (Postman / Swagger UI)
        │
        ▼
Security Filter (JWT validation)
        │
        ▼
Controller  →  Service  →  Repository  →  MySQL
 (REST API)    (business     (Spring Data
                logic)        JPA / Hibernate)
```

- **Controller:** Handles HTTP requests and responses, and validates input.
- **Service:** Contains the business logic.
- **Repository:** Handles database access through Spring Data JPA.
- **DTOs:** Keep the API contract separate from the database entities.
- **Exception handler:** Converts errors into consistent JSON responses with proper HTTP status codes.

## Authentication and Authorization

1. A user logs in with valid credentials and receives a JWT.
2. The client sends the token with each request: `Authorization: Bearer <token>`.
3. A security filter validates the token and loads the user's role.
4. Spring Security allows or denies access based on the role.

| Role | Access |
|---|---|
| `ROLE_ADMIN` | Full access to student management |
| `ROLE_USER` | Read-only access |

## API Overview

Full interactive documentation is available in Swagger UI (see below). A summary of the main endpoints:

| Method | Endpoint | Description | Access |
|---|---|---|---|
| POST | `/api/auth/register` | Register a new user | Public |
| POST | `/api/auth/login` | Log in and receive a JWT | Public |
| GET | `/api/students` | List students (supports pagination and sorting) | `Authenticated` |
| GET | `/api/students/{id}` | Get a student by ID | `Authenticated` |
| POST | `/api/students` | Create a student | `Admin` |
| PUT | `/api/students/{id}` | Update a student | `Admin` |
| DELETE | `/api/students/{id}` | Delete a student | `Admin` |

Example of pagination and sorting:

```
GET /api/students?page=0&size=10&sort=name,asc
```

## Getting Started

### Prerequisites

- JDK 17+
- Maven
- MySQL

### Setup

1. **Clone the repository**

   ```bash
   git clone https://github.com/DevXRishi/student-management-system.git
   cd student-management-system
   ```

2. **Create the database**

   ```sql
   CREATE DATABASE studentdb;
   ```

3. **Configure the application** in `src/main/resources/application.properties`:

   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/[educore]
   spring.datasource.username=${DB_USERNAME}
   spring.datasource.password=${DB_PASSWORD}
   spring.jpa.hibernate.ddl-auto=update

   # Use a long, random secret and keep it out of version control
   jwt.secret=${JWT_SECRET}
   ```

   Set `DB_USERNAME`, `DB_PASSWORD` and `JWT_SECRET` as environment variables. Adjust the property names to match your project.

4. **Run the application**

   ```bash
   mvn spring-boot:run
   ```

   The API starts at `http://localhost:8080`.

### API Documentation

Once the application is running, open Swagger UI at:

```
http://localhost:8080/swagger-ui/index.html
```

Use the **Authorize** button and paste your JWT to try the protected endpoints.

## Running the Tests

```bash
mvn test
```

The test suite covers:

- **Service layer:** unit tests with Mockito
- **Controller layer:** API tests with MockMvc, including positive and negative cases
- **Security:** authentication and role-based access checks

## Author

**Rushikesh Bhandekar**
Java Backend Developer | B.Tech Computer Engineering

- GitHub: DevXRishi(https://github.com/DevXRishi)
- LinkedIn:  https://www.linkedin.com/in/rushikesh-bhandekar-7a462635b/
- Email: rushikeshbh18@gmail.com
