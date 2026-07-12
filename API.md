# NovaPOS Backend — API Conventions

## 1. URL & Resource Conventions

- Base path: `/api/v1`.
- Resource-oriented, plural nouns: `/api/v1/branches/{branchId}/sales`, `/api/v1/products`.
- Every module's controllers live in `com.novapos.<module>.web`, mapped under a module-specific path prefix where it aids clarity (e.g. `/api/v1/pos/...`, `/api/v1/inventory/...`).
- List endpoints support `?page=&size=&sort=field,asc|desc` (Spring Data `Pageable`, bound automatically) and `?filter[field]=value` via a custom `@ModelAttribute` filter DTO per endpoint — do not hand-roll pagination.

## 2. Request/Response Shape

- JSON in, JSON out. Request/response DTOs are separate classes from JPA entities — controllers never accept or return entity classes directly.
- Money fields are always `*_minor` integers in JSON (e.g. `"totalMinor": 9072`), never floats. The frontend formats for display later; this backend never emits a pre-formatted currency string.
- Timestamps are ISO-8601 UTC (`"2026-07-11T10:15:00Z"`).

## 3. Status Codes

| Code | Meaning |
|---|---|
| 200 | Successful GET/PUT/PATCH |
| 201 | Successful POST that created a resource (`Location` header set) |
| 204 | Successful DELETE or action with no response body |
| 400 | Validation failure |
| 401 | Missing/invalid auth |
| 403 | Authenticated but not permitted (role/permission check failed) |
| 404 | Resource not found |
| 409 | Conflict (e.g. idempotency key reuse with a different payload, optimistic lock failure) |
| 422 | Semantically invalid request the validator can't catch (e.g. "return quantity exceeds original sale quantity") |
| 429 | Rate limited |
| 500 | Unhandled server error — must never leak a stack trace to the client |

## 4. Error Response Format

Every non-2xx response uses this shape, produced by a single global `@RestControllerAdvice`:

```json
{
  "code": "INSUFFICIENT_STOCK",
  "message": "Only 3 units of SKU-00214 are available at this branch.",
  "details": { "productVariantId": "…", "available": 3, "requested": 5 },
  "traceId": "a1b2c3"
}
```

- `code` is a stable, machine-readable enum-like string — client code branches on this, never on `message` text.
- Every module defines its own error codes in `com.novapos.<module>.web.<Module>ErrorCode`, and the global exception handler maps module-specific exceptions to the right HTTP status + code.
- `message` is human-readable and safe to display; it never includes internal identifiers, stack traces, or SQL.

## 5. Authentication & Authorization

- Stateless JWT bearer tokens (`Authorization: Bearer <token>`), issued by the `user_access` module's `/api/v1/auth/login` and `/api/v1/auth/refresh` endpoints.
- PIN login for shared POS terminals is a separate endpoint (`/api/v1/auth/pin-login`) that additionally requires a registered `terminalId` — see `TASKS.md` Task 3.2.
- Every controller method is annotated with a method-security check (`@PreAuthorize("hasPermission(...)")`) mapped to the permission matrix in the companion technical design doc — this is checked on every request, never cached beyond token expiry.
- Branch-scoped roles: the JWT includes the user's role assignments (role + branch scope); a Spring Security `PermissionEvaluator` implementation checks the requested resource's `branchId` against the caller's scope on every request, not just at login.

## 6. Idempotency

- Any endpoint that creates a financial or stock-affecting record accepts an `Idempotency-Key` header (or, for POS sales specifically, the `clientUuid` field in the payload — see `pos.sale.client_uuid` in `DATABASE.md`).
- The server persists a mapping of idempotency key → response for a rolling window (24h minimum) so a retried request with the same key returns the original result (200/201) rather than creating a duplicate; a retried request with the same key but a *different* payload returns 409.

## 7. OpenAPI

- springdoc-openapi generates the spec from controller annotations automatically — do not hand-write a separate OpenAPI YAML that can drift from the code.
- Every DTO field has a `@Schema(description = "...")` where the meaning isn't obvious from the name alone (e.g. clarify `costingMethod` accepts `FIFO|LIFO|AVERAGE`).
- `/swagger-ui.html` must be reachable in `local` and `staging` profiles; disabled in `prod`.

## 8. Versioning

- URL-path versioning (`/api/v1/...`). A breaking change to a module's public contract ships as `/api/v2/...` for that module's endpoints specifically — the whole API does not need to version-bump together, since modules are independent.
