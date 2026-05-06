# BankX

A banking system backend built with **Spring Boot 3 + JPA + Spring Security + Redis**, featuring Permission-Based Access Control (PBAC) with role-scoped URL routing.

## Tech Stack

- **Java 17 / Spring Boot 3**
- **Spring Security** — JWT + Redis token blacklist
- **Spring Data JPA** — PostgreSQL, Hibernate
- **Redis** — token storage, email verification code, rate limiting
- **Swagger / OpenAPI 3** — API documentation
- **Optimistic Locking + @Retryable** — concurrent transaction safety
- **Idempotency Key** — duplicate transaction prevention

## Architecture

Controllers are split by **role-based URL prefix**, so each frontend page maps 1:1 to a URL path:

```
/api/v1/me/**        → Customer self-service
/api/v1/teller/**    → Teller operations
/api/v1/manager/**   → Manager operations
/api/v1/admin/**     → Admin operations
```

Authorization is enforced at two layers:
1. **URL-level** — `SecurityConfig` restricts path prefixes (e.g. `/api/v1/admin/**` requires authentication)
2. **Method-level** — `@PreAuthorize("hasAuthority('ACCOUNT:FREEZE')")` enforces fine-grained PBAC

---

## API Reference

### Public Endpoints (no auth required)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/auth/register` | Register new customer |
| POST | `/api/v1/auth/login` | Login and get JWT |
| POST | `/api/v1/auth/logout` | Logout and invalidate token |
| POST | `/api/v1/auth/send-code` | Send email verification code |

### Customer — `/api/v1/me/`

| Method | Path | Permission | Description |
|--------|------|------------|-------------|
| POST | `/me/accounts` | ACCOUNT:CREATE | Create account |
| GET | `/me/accounts` | ACCOUNT:READ_OWN | List my accounts |
| GET | `/me/accounts/{accountId}` | ACCOUNT:READ_OWN | Get account detail |
| GET | `/me/transactions` | TRANSACTION:READ_OWN | Search my transactions (paginated) |
| GET | `/me/transactions/{accountId}` | TRANSACTION:READ_OWN | Get transaction history for account |
| POST | `/me/transactions/deposit` | TRANSACTION:CREATE | Deposit |
| POST | `/me/transactions/withdraw` | TRANSACTION:CREATE | Withdraw |
| POST | `/me/transactions/transfer` | TRANSACTION:CREATE | Transfer |

### User Profile — `/api/v1/profile/`

| Method | Path | Permission | Description |
|--------|------|------------|-------------|
| GET | `/profile` | USER_PROFILE:READ_OWN | Get my profile |
| PUT | `/profile/update` | USER_PROFILE:UPDATE | Update profile (email excluded) |
| POST | `/profile/email/request` | USER_PROFILE:UPDATE | Request email change → send code |
| PUT | `/profile/email/confirm` | USER_PROFILE:UPDATE | Confirm code → update email + reissue token |

### Teller — `/api/v1/teller/`

| Method | Path | Permission | Description |
|--------|------|------------|-------------|
| GET | `/teller/accounts` | ACCOUNT:READ_ALL | List all accounts |
| GET | `/teller/transactions` | TRANSACTION:READ_ALL | List all transactions |

### Manager — `/api/v1/manager/`

| Method | Path | Permission | Description |
|--------|------|------------|-------------|
| PATCH | `/manager/accounts/{accountId}/freeze` | ACCOUNT:FREEZE | Freeze account |

### Admin — `/api/v1/admin/`

| Method | Path | Permission | Description |
|--------|------|------------|-------------|
| PATCH | `/admin/accounts/{accountId}/unfreeze` | ACCOUNT:UNFREEZE | Unfreeze account |
| PATCH | `/admin/accounts/{accountId}/close` | ACCOUNT:CLOSE | Close account (soft delete) |
| PATCH | `/admin/accounts/{accountId}/unclose` | ACCOUNT:CLOSE | Reopen closed account |
| POST | `/admin/transactions/{transactionId}/reverse` | TRANSACTION:REVERSE | Reverse a transaction |
| GET | `/admin/users` | USER:READ_ALL | List all users |
| GET | `/admin/users/{id}` | USER:READ_ALL | Get user detail |
| GET | `/admin/users/{id}/profile` | USER:READ_ALL | Get user profile |
| PATCH | `/admin/users/{id}/freeze` | USER:FREEZE | Freeze user |
| PATCH | `/admin/users/{id}/unfreeze` | USER:FREEZE | Unfreeze user |
| PATCH | `/admin/users/{id}/role` | USER:ASSIGN_ROLE | Assign role to user |

---

## Role & Permission Matrix

| Permission | CUSTOMER | TELLER | MANAGER | ADMIN |
|---|:---:|:---:|:---:|:---:|
| **ACCOUNT** | | | | |
| ACCOUNT:READ_OWN | ✅ | ✅ | ✅ | ✅ |
| ACCOUNT:READ_ALL | | ✅ | ✅ | ✅ |
| ACCOUNT:CREATE | ✅ | ✅ | ✅ | ✅ |
| ACCOUNT:UPDATE | | | | ✅ |
| ACCOUNT:FREEZE | | | ✅ | ✅ |
| ACCOUNT:UNFREEZE | | | | ✅ |
| ACCOUNT:CLOSE | | | | ✅ |
| **TRANSACTION** | | | | |
| TRANSACTION:CREATE | ✅ | ✅ | ✅ | ✅ |
| TRANSACTION:READ_OWN | ✅ | ✅ | ✅ | ✅ |
| TRANSACTION:READ_ALL | | ✅ | ✅ | ✅ |
| TRANSACTION:REVERSE | | | | ✅ |
| **AUDIT_LOG** | | | | |
| AUDIT_LOG:READ | | | ✅ | ✅ |
| AUDIT_LOG:EXPORT | | | | ✅ |
| **USER** | | | | |
| USER:READ_OWN | | | | ✅ |
| USER:READ_ALL | | | | ✅ |
| USER:CREATE | | | | ✅ |
| USER:UPDATE | | | | ✅ |
| USER:DELETE | | | | ✅ |
| USER:ASSIGN_ROLE | | | | ✅ |
| USER:FREEZE | | | | ✅ |
| **USER_PROFILE** | | | | |
| USER_PROFILE:READ_OWN | | | | ✅ |
| USER_PROFILE:UPDATE | | | | ✅ |

---

## Getting Started

### Prerequisites

- Java 17+
- PostgreSQL
- Redis

### Environment Variables

Create a `.env` file in the project root:

```properties
PGDB_USERNAME=your_pg_username
PGDB_PASSWORD=your_pg_password
REDIS_PASSWORD=your_redis_password
ENCODED_SECRET_KEY=your_jwt_secret_key_at_least_32_chars
MAIL_USERNAME=your_email@gmail.com
GMAIL_APP_PASSWORD=your_gmail_app_password
```

### Run

```bash
./mvnw spring-boot:run
```

Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
