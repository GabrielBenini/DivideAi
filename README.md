# DivideAí — Shared Expense Management API

A RESTful API developed with **Java 21, Spring Boot 4, and PostgreSQL** to simplify shared expense management.

DivideAí allows users to create groups, register shared expenses, automatically calculate individual balances, and generate settlement suggestions to determine who owes money to whom.

The backend is designed to integrate with a native Android application built using **Kotlin and Jetpack Compose**.

## Features

- **User Management:** Create, retrieve, update, and delete users.
- **Group Management:** Create groups and manage participants.
- **Expense Management:** Register, update, list, and delete shared expenses.
- **Equal Expense Splitting:** Automatically distribute expenses among all group members.
- **Balance Calculation:** Determine how much each participant has paid, owes, or should receive.
- **Settlement Suggestions:** Generate transfers between debtors and creditors.
- **Business Rule Validation:** Prevent invalid operations and enforce group membership requirements.
- **Centralized Exception Handling:** Standardized HTTP error responses.
- **Swagger Documentation:** Interactive API documentation and endpoint testing.

## Technologies

| Technology              | Purpose                   |
| ----------------------- | ------------------------- |
| Java 21                 | Programming language      |
| Spring Boot 4.1.2       | Backend framework         |
| Spring Web MVC          | RESTful API development   |
| Spring Data JPA         | Data persistence          |
| Hibernate ORM           | Object-relational mapping |
| PostgreSQL              | Development database      |
| H2 Database             | Automated testing         |
| Maven                   | Dependency management     |
| Lombok                  | Boilerplate reduction     |
| Jakarta Bean Validation | Request validation        |
| Springdoc OpenAPI       | Swagger documentation     |
| JUnit 5 / Mockito       | Automated testing         |

## Project Architecture

The backend follows a feature-based modular architecture, separating business logic, persistence, API contracts, and HTTP handling.

```text
src/
├── main/
│   ├── java/com/github/gabriel/benini/DivideAi/
│   │   ├── user/
│   │   │   ├── dto/
│   │   │   ├── User.java
│   │   │   ├── UserController.java
│   │   │   ├── UserService.java
│   │   │   └── UserRepository.java
│   │   ├── group/
│   │   │   ├── dto/
│   │   │   ├── Group.java
│   │   │   ├── GroupMember.java
│   │   │   ├── GroupController.java
│   │   │   ├── GroupService.java
│   │   │   ├── GroupRepository.java
│   │   │   └── GroupMemberRepository.java
│   │   ├── expense/
│   │   │   ├── dto/
│   │   │   ├── Expense.java
│   │   │   ├── ExpenseController.java
│   │   │   ├── ExpenseService.java
│   │   │   └── ExpenseRepository.java
│   │   ├── balance/
│   │   │   ├── dto/
│   │   │   ├── BalanceController.java
│   │   │   ├── BalanceService.java
│   │   │   └── TransferService.java
│   │   └── common/
│   │       └── exception/
│   └── resources/
│       ├── application.yaml
│       ├── application-dev.yaml
│       └── application-prod.yaml
└── test/
```

### Architecture Principles

- Feature-based package organization
- Separation of concerns
- Controller, Service, and Repository layers
- DTO-based request and response contracts
- Dependency injection
- Transactional operations
- Centralized exception handling
- Business logic independent of HTTP controllers

## Business Rules

### Group Membership

- A group is created with its creator as the first member.
- Users can join multiple groups.
- Duplicate membership is not allowed.
- A group cannot be left without members.
- Members responsible for registered expenses cannot be removed while those expenses exist.

### Expense Registration

- Every expense belongs to a group.
- Every expense has a responsible payer.
- The payer must be a member of the group.
- Expense amounts must be positive.
- Monetary amounts are stored as integer cents to avoid floating-point precision issues.

### Equal Expense Splitting

Expenses are equally divided among all members of the group.

If the amount cannot be divided exactly, remaining cents are distributed deterministically according to ascending user IDs.

### Balance Calculation

Each participant's balance is calculated using:

```text
Balance = Total Paid - Individual Share
```

- **Positive balance:** The participant should receive money.
- **Negative balance:** The participant owes money.
- **Zero balance:** The participant has settled their share.

### Settlement Algorithm

DivideAí implements a greedy settlement algorithm that:

1. Identifies participants with outstanding debts.
2. Identifies participants who should receive money.
3. Matches debtors and creditors.
4. Generates transfer suggestions until all balances are settled.

Settlement calculations are performed on demand. No real financial transactions are processed, and settlements are not persisted.

## API Endpoints

### Users

| Method | Endpoint          | Description     |
| ------ | ----------------- | --------------- |
| POST   | `/api/users`      | Create a user   |
| GET    | `/api/users`      | List users      |
| GET    | `/api/users/{id}` | Retrieve a user |
| PUT    | `/api/users/{id}` | Update a user   |
| DELETE | `/api/users/{id}` | Delete a user   |

### Groups

| Method | Endpoint                                 | Description         |
| ------ | ---------------------------------------- | ------------------- |
| POST   | `/api/groups`                            | Create a group      |
| GET    | `/api/groups`                            | List groups         |
| GET    | `/api/groups?userId={id}`                | List groups by user |
| GET    | `/api/groups/{groupId}`                  | Retrieve a group    |
| PUT    | `/api/groups/{groupId}`                  | Update a group      |
| DELETE | `/api/groups/{groupId}`                  | Delete a group      |
| GET    | `/api/groups/{groupId}/members`          | List members        |
| POST   | `/api/groups/{groupId}/members`          | Add a member        |
| DELETE | `/api/groups/{groupId}/members/{userId}` | Remove a member     |

### Expenses

| Method | Endpoint                             | Description         |
| ------ | ------------------------------------ | ------------------- |
| POST   | `/api/expenses`                      | Register an expense |
| GET    | `/api/expenses/{id}`                 | Retrieve an expense |
| GET    | `/api/expenses?groupId={id}`         | List group expenses |
| PUT    | `/api/expenses/{id}`                 | Update an expense   |
| DELETE | `/api/expenses/{id}`                 | Delete an expense   |
| GET    | `/api/expenses/summary?groupId={id}` | Get expense summary |

### Balances and Settlements

| Method | Endpoint                           | Description                     |
| ------ | ---------------------------------- | ------------------------------- |
| GET    | `/api/groups/{groupId}/balance`    | Calculate group balances        |
| GET    | `/api/groups/{groupId}/settlement` | Generate settlement suggestions |

## Example: Splitting a Group Expense

Consider a group with three participants: Alice, Bob, and Charlie.

Alice pays **$90.00** for a shared expense.

Each participant's share is **$30.00**.

| Participant | Amount Paid | Individual Share | Balance |
| ----------- | ----------: | ---------------: | ------: |
| Alice       |      $90.00 |           $30.00 | +$60.00 |
| Bob         |       $0.00 |           $30.00 | -$30.00 |
| Charlie     |       $0.00 |           $30.00 | -$30.00 |

The settlement algorithm generates:

```text
Bob     → Alice: $30.00
Charlie → Alice: $30.00
```

This allows participants to settle their expenses without manually calculating individual debts.

## Getting Started

### Prerequisites

- Java 21
- PostgreSQL
- Git
- IntelliJ IDEA or another Java IDE

### Clone the Repository

```bash
git clone git@github.com:gabrielbenini/DivideAi.git
cd DivideAi
```

### PostgreSQL Configuration

Create a PostgreSQL user and database using an administrative connection:

```sql
CREATE USER divideai_user
WITH PASSWORD 'your_secure_password';

CREATE DATABASE divideai_db
OWNER divideai_user;
```

The application uses environment variables to avoid exposing database credentials.

Configure:

```text
DB_USERNAME=divideai_user
DB_PASSWORD=your_secure_password
```

For IntelliJ IDEA, these variables can be configured under **Run → Edit Configurations → Environment variables**.

### Application Configuration

The development profile uses PostgreSQL.

**application-dev.yaml**

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/divideai_db
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    open-in-view: false

server:
  port: 8080
```

The `update` setting is intended for local development only.

Production environments should use controlled database migrations.

### Run the Application

Using the Maven Wrapper:

```bash
./mvnw spring-boot:run
```

Or run the `DivideAiApplication` class directly from IntelliJ IDEA.

The API will be available at:

```text
http://localhost:8080
```

## API Documentation

Swagger UI is automatically generated using Springdoc OpenAPI.

**Swagger UI:**

```text
http://localhost:8080/swagger-ui/index.html
```

**OpenAPI JSON:**

```text
http://localhost:8080/v3/api-docs
```

Swagger allows developers to explore endpoints, inspect request and response models, and execute HTTP requests directly.

## Database

DivideAí uses PostgreSQL for persistent development data storage.

The database schema contains four main tables:

```text
divideai_db
└── public
    ├── users
    ├── groups
    ├── group_members
    └── expenses
```

### Relationships

- A user can participate in multiple groups.
- A group can contain multiple users.
- Group membership is represented by `group_members`.
- A group can contain multiple expenses.
- Each expense references its payer and group.

## Automated Testing

The project includes automated tests using JUnit 5, Mockito, Spring Boot Test, and H2.

Run the test suite using:

```bash
./mvnw clean verify
```

The tests cover business logic, validation, expense management, balance calculation, settlement suggestions, and API integration.

Final test-suite verification is pending.

## Android Integration

The next development phase is a native Android application built with:

- Kotlin
- Jetpack Compose
- Material 3
- MVVM
- Clean Architecture principles
- Retrofit / OkHttp
- Hilt
- Navigation
- DataStore

During local development, the Android Emulator can communicate with the backend through:

```text
http://10.0.2.2:8080/
```

The mobile application will allow users to create groups, register expenses, visualize balances, and generate settlement suggestions.

## Security

This project is currently intended for portfolio and development purposes.

Authentication and authorization have not yet been implemented.

User IDs are not a substitute for authentication, and the API should not be publicly exposed with real user data in its current state.

Database credentials are provided through environment variables and should never be committed to version control.

## Roadmap

- [x] Spring Boot REST API
- [x] User management
- [x] Group and membership management
- [x] Expense management
- [x] Equal expense splitting
- [x] Balance calculation
- [x] Settlement algorithm
- [x] Global exception handling
- [x] Swagger / OpenAPI documentation
- [x] PostgreSQL integration
- [x] Development and production configuration profiles
- [x] Git ignore configuration
- [x] Final automated test verification
- [ ] Native Android application
- [ ] Docker configuration and testing
- [ ] Database migrations
- [ ] Production deployment

## Project Status

**Backend implemented and PostgreSQL integration validated.**

The project is transitioning to native Android development. Docker configuration, production deployment, and final automated test verification remain planned work.
