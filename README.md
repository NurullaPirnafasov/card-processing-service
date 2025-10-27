# Card Processing Service

A backend service for managing card operations including creation, fund transfers, blocking/unblocking, and transaction history.

## Table of Contents

1. Overview
2. Technologies
3. Setup and Installation
4. API Endpoints
5. Authentication
6. Database
7. Logging
8. External Integration
9. Future Enhancements

---

## Overview

This service allows users to:

* Create a new card
* Withdraw funds from the card
* Top up funds to the card
* Block and unblock the card
* Retrieve transaction history

The service ensures:

* Idempotency for sensitive operations
* Proper status handling (ACTIVE, BLOCKED, CLOSED)
* Currency exchange handling via CBU API if needed

---

## Technologies

* Programming Language: Java 17
* Framework: Spring Boot 3
* Database: PostgreSQL
* Build Tool: Maven
* Version Control: GitLab
* Migration: Liquibase
* Logging: log4j2
* API Integration: CBU exchange rate API

> Note: Docker support is available via `docker-compose` for future deployment.

---

## Setup and Installation

1. Clone repository:
   `git clone <repo-url>`
   `cd card-processing-service`

2. Configure application properties:
   Set database, JWT secret, and CBU API URL in `application.yml` or environment variables.

3. Database migration:
   Liquibase will automatically create tables and insert initial data on startup.

4. Run the application:
   `mvn clean spring-boot:run`

The service will start on `http://localhost:8080`

---

## API Endpoints

All APIs are prefixed with `/api/v1/cards`.

1. **Create New Card**

   * POST `/api/v1/cards`
   * Requires `Idempotency-Key` header
   * Returns card details including unique `card_id`

2. **Get Card**

   * GET `/api/v1/cards/{cardId}`
   * Returns card details with `ETag` for block/unblock operations

3. **Block Card**

   * POST `/api/v1/cards/{cardId}/block`
   * Requires `If-Match` header with last ETag
   * Changes card status to `BLOCKED`

4. **Unblock Card**

   * POST `/api/v1/cards/{cardId}/unblock`
   * Requires `If-Match` header with last ETag
   * Changes card status to `ACTIVE`

5. **Withdraw Funds**

   * POST `/api/v1/cards/{cardId}/debit`
   * Requires `Idempotency-Key` header
   * Handles currency exchange if needed
   * Returns transaction details and updated balance

6. **Top Up Funds**

   * POST `/api/v1/cards/{cardId}/credit`
   * Requires `Idempotency-Key` header
   * Handles currency exchange if needed
   * Returns transaction details and updated balance

7. **Get Transaction History**

   * GET `/api/v1/cards/{cardId}/transactions`
   * Supports filtering by type, transaction_id, external_id, currency
   * Supports pagination with `page` and `size`

> Detailed request/response formats are available in Swagger UI.

---

## Authentication

JWT-based authentication is implemented.

* Include `Authorization: Bearer <token>` in all requests
* Users are authorized based on roles and card ownership

---

## Database

* PostgreSQL is used as the main database.
* Tables are created and seeded using Liquibase.
* Sensitive data (e.g., card numbers) is encrypted.

---

## Logging

* Configured using log4j2
* Logs include timestamp, log level, thread, and message
* Important events (card creation, transactions) are logged for auditing

---

## External Integration

* Exchange rates fetched from [CBU API](https://cbu.uz/ru/arkhiv-kursov-valyut/veb-masteram/)
* Currency conversion applied automatically for debit/credit operations


Agar xohlasang, men **README ni yanada professional qilib, API request/response example’lari bilan to‘liq text versiyada** ham tayyorlab bera olaman. Shu qilaylikmi?
