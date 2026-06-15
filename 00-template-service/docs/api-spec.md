# API Specification — customer-service

Base URL: `http://localhost:8080`
Interactive docs: Swagger UI at `/swagger-ui.html`, OpenAPI JSON at `/v3/api-docs`.

All responses are JSON and wrapped in a standard envelope.

## Envelopes

**Success** — `ApiResponse<T>`:

```json
{ "success": true, "data": { }, "message": "Customer created successfully" }
```

`message` may be `null` (e.g. simple reads); `data` may be `null` (e.g. delete).

**Error** — `ErrorResponse` (null fields omitted):

```json
{
  "success": false,
  "errorCode": "CUSTOMER_NOT_FOUND",
  "message": "Customer not found with id: '42'",
  "timestamp": "2026-06-14T10:15:30Z",
  "path": "/api/v1/customers/42"
}
```

Validation errors add a per-field `errors` array:

```json
{
  "success": false,
  "errorCode": "VALIDATION_ERROR",
  "message": "Validation failed for one or more fields",
  "timestamp": "2026-06-14T10:15:30Z",
  "path": "/api/v1/customers",
  "errors": [ { "field": "email", "message": "must not be blank" } ]
}
```

Every response also carries an **`X-Trace-Id`** header for log correlation.

## Resource: Customer

`CustomerRequest` (input):

| Field | Type | Constraints |
|---|---|---|
| `firstName` | string | required, ≤ 100 |
| `lastName` | string | required, ≤ 100 |
| `email` | string | required, valid email, ≤ 320 |
| `phone` | string | optional, ≤ 30 |
| `status` | enum | optional (`ACTIVE`/`INACTIVE`/`SUSPENDED`); defaults to `ACTIVE` on create |

`CustomerResponse` (output): `id, firstName, lastName, email, phone, status, createdAt, updatedAt`.

## Endpoints

### Create — `POST /api/v1/customers`
- Body: `CustomerRequest`
- **201 Created** → `ApiResponse<CustomerResponse>`
- **400** `VALIDATION_ERROR` — invalid body
- **409** `CUSTOMER_EMAIL_EXISTS` — email already in use

```bash
curl -X POST http://localhost:8080/api/v1/customers \
  -H 'Content-Type: application/json' \
  -d '{"firstName":"Ada","lastName":"Lovelace","email":"ada@example.com","phone":"+1-555-0100"}'
```

### Get by id — `GET /api/v1/customers/{id}`
- **200 OK** → `ApiResponse<CustomerResponse>`
- **404** `CUSTOMER_NOT_FOUND`

### List (paged) — `GET /api/v1/customers`
- Query params: `page` (default 0), `size` (default 20), `sort` (e.g. `sort=lastName,asc`)
- **200 OK** → `ApiResponse<PagedModel<CustomerResponse>>`

```json
{
  "success": true,
  "data": {
    "content": [ { "id": 1, "email": "ada@example.com", "status": "ACTIVE" } ],
    "page": { "size": 20, "number": 0, "totalElements": 1, "totalPages": 1 }
  },
  "message": null
}
```

### Update — `PUT /api/v1/customers/{id}`
- Body: `CustomerRequest`
- **200 OK** → `ApiResponse<CustomerResponse>`
- **400** `VALIDATION_ERROR`, **404** `CUSTOMER_NOT_FOUND`, **409** `CUSTOMER_EMAIL_EXISTS` (email taken by another customer)

### Delete — `DELETE /api/v1/customers/{id}`
- **200 OK** → `ApiResponse<Void>` with message `"Customer deleted successfully"`
- **404** `CUSTOMER_NOT_FOUND`

## Error codes

| Code | HTTP | Meaning |
|---|---|---|
| `VALIDATION_ERROR` | 400 | Request failed validation (see `errors`) |
| `CUSTOMER_NOT_FOUND` | 404 | No customer with the given id |
| `CUSTOMER_EMAIL_EXISTS` | 409 | Email already used by another customer |
| `BUSINESS_RULE_VIOLATION` | 409 | Generic business-rule violation |
| `INTERNAL_ERROR` | 500 | Unexpected error (details logged, not exposed) |

## Operational endpoints

| Endpoint | Purpose |
|---|---|
| `/actuator/health` | Liveness/readiness (`UP` only when DB reachable) |
| `/actuator/info` | Build/app info |
| `/actuator/metrics` | Micrometer metrics |
