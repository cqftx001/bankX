# BankX

A full-stack banking system built with **Spring Boot 3 + JPA + Spring Security + Redis**, featuring Permission-Based Access Control (PBAC) with role-scoped URL routing.

## Tech Stack

**Backend:** Java 17, Spring Boot 3, Spring Security (JWT + Redis blacklist), Spring Data JPA, PostgreSQL, Redis, Swagger/OpenAPI 3

**Frontend:** React 19, TypeScript, Vite, Ant Design, Zustand, i18next, Axios

**Key Design Patterns:**
- Optimistic locking + `@Retryable` for concurrent transaction safety
- Idempotency key with DB unique constraint for duplicate prevention
- JPA Specification for dynamic query composition
- Tiered profile field protection (KYC compliance)

---

## Architecture

Controllers are split by **role-based URL prefix**, so each frontend page maps 1:1 to a URL path:

```
/api/v1/me/**        → Customer self-service
/api/v1/teller/**    → Teller operations (paginated search with filters)
/api/v1/manager/**   → Manager operations
/api/v1/admin/**     → Admin operations
```

Authorization is enforced at two layers:
1. **URL-level** — `SecurityConfig` restricts path prefixes by authentication
2. **Method-level** — `@PreAuthorize("hasAuthority('...')")` enforces fine-grained PBAC

---

## API Reference

### Auth — `/api/v1/auth`

| Method | Path | Auth | Description |
|--------|------|:----:|-------------|
| POST | `/auth/register` | — | Register new customer |
| POST | `/auth/login` | — | Login, receive JWT |
| POST | `/auth/logout` | ✅ | Logout, invalidate token |
| POST | `/auth/send-code` | — | Send email verification code |

### Customer Accounts — `/api/v1/me/accounts`

| Method | Path | Permission | Description |
|--------|------|------------|-------------|
| POST | `/me/accounts` | ACCOUNT:CREATE | Create account |
| GET | `/me/accounts` | ACCOUNT:READ_OWN | List my accounts |
| GET | `/me/accounts/{id}` | ACCOUNT:READ_OWN | Get account detail |

### Customer Transactions — `/api/v1/me/transactions`

| Method | Path | Permission | Description |
|--------|------|------------|-------------|
| GET | `/me/transactions` | TRANSACTION:READ_OWN | Search my transactions (paginated + filters) |
| GET | `/me/transactions/{accountId}` | TRANSACTION:READ_OWN | Transaction history for account |
| POST | `/me/transactions/deposit` | TRANSACTION:CREATE | Deposit |
| POST | `/me/transactions/withdraw` | TRANSACTION:CREATE | Withdraw |
| POST | `/me/transactions/transfer` | TRANSACTION:CREATE | Transfer between accounts |

### Customer Profile — `/api/v1/me/profile`

| Method | Path | Permission | Description |
|--------|------|------------|-------------|
| GET | `/me/profile` | USER_PROFILE:READ_OWN | Get my profile |
| PUT | `/me/profile/update` | USER_PROFILE:UPDATE | Update address fields (Tier 1 only) |
| POST | `/me/profile/email/request` | USER_PROFILE:UPDATE | Request email change → send verification code |
| PUT | `/me/profile/email/confirm` | USER_PROFILE:UPDATE | Confirm code → update email + reissue token |

### Teller — `/api/v1/teller`

| Method | Path | Permission | Description |
|--------|------|------------|-------------|
| GET | `/teller/accounts` | ACCOUNT:READ_ALL | Search all accounts (paginated + filters) |
| GET | `/teller/transactions` | TRANSACTION:READ_ALL | Search all transactions (paginated + filters) |

### Manager — `/api/v1/manager`

| Method | Path | Permission | Description |
|--------|------|------------|-------------|
| PATCH | `/manager/accounts/{id}/freeze` | ACCOUNT:FREEZE | Freeze account |
| PUT | `/manager/profile/{userId}/update` | USER_PROFILE:UPDATE_ALL | Update user profile (all fields + email) |

### Admin — `/api/v1/admin`

| Method | Path | Permission | Description |
|--------|------|------------|-------------|
| PATCH | `/admin/accounts/{id}/unfreeze` | ACCOUNT:UNFREEZE | Unfreeze account |
| PATCH | `/admin/accounts/{id}/close` | ACCOUNT:CLOSE | Close account (soft delete) |
| PATCH | `/admin/accounts/{id}/unclose` | ACCOUNT:CLOSE | Reopen closed account |
| POST | `/admin/transactions/{id}/reverse` | TRANSACTION:REVERSE | Reverse a transaction |

---

## Profile Field Tiering (KYC Compliance)

Profile fields are classified by modification permission to meet banking KYC requirements:

| Field | Customer | Manager+ | Rationale |
|-------|:--------:|:--------:|-----------|
| addressLine1/2, city, state, zipCode, country | ✅ | ✅ | Moving is routine, no identity impact |
| firstName, lastName, phone, birthDate | ❌ | ✅ | KYC-verified at registration, requires branch visit |
| email | ✅ (verification code) | ✅ (direct) | Customer uses 2-step flow; Manager verifies identity in person |

Protection is enforced at three layers:
1. **UI** — KYC fields render as read-only with lock icon
2. **API contract** — `UpdateMyProfileRequest` DTO excludes KYC fields entirely
3. **Service** — Method signature accepts only Tier 1 DTO, physically cannot receive sensitive fields

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
| USER_PROFILE:READ_OWN | ✅ | | ✅ | ✅ |
| USER_PROFILE:UPDATE | ✅ | | ✅ | ✅ |
| USER_PROFILE:UPDATE_ALL | | | ✅ | ✅ |

---

## Getting Started

### Prerequisites

- Java 17+
- PostgreSQL
- Redis

### Environment Variables

```properties
PGDB_USERNAME=your_pg_username
PGDB_PASSWORD=your_pg_password
REDIS_PASSWORD=your_redis_password
ENCODED_SECRET_KEY=your_jwt_secret_key_at_least_32_chars
MAIL_USERNAME=your_email@gmail.com
GMAIL_APP_PASSWORD=your_gmail_app_password
```

### Run Backend

```bash
cd demo
./mvnw spring-boot:run
```

Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### Run Frontend

```bash
cd bankx-web
npm install
npm run dev
```

Frontend: [http://localhost:5173](http://localhost:5173) (proxies `/api` to backend)

---

## Future Improvements

- **Multi-currency support** — Currency is modeled as an enum at the entity level, reserved for future expansion. A complete implementation would require a ForexService with external rate API, daily EOD rate snapshots, cross-currency transfer accounting with forex spread, and reversal logic with original-rate vs current-rate handling.

- **P2P transfer by identifier (Zelle-like)** — Current transfer uses fromAccountId/toAccountId, suitable for between-own-accounts scenarios. A P2P flow would resolve recipients by phone/email, route to their default receive account, enforce AML daily/monthly limits, and handle "recipient not yet registered" claim windows. Architecture: a P2PTransferService orchestrator layered above the existing TransactionService.transfer() primitive.

- **Audit log system** — Permission and entity models are in place. Full implementation would include operation logging, search/export functionality, and compliance reporting.
