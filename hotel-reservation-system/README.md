# Hotel Reservation System Backend

Backend-only REST API for Java 21, Spring Boot 3.4, Maven, Spring Data JPA and SQLite. This is a local/demo project, not a production deployment. Public API payloads are JSON unless an endpoint explicitly exports CSV.

## Run locally

1. Install JDK 21 and Maven 3.9+.
2. Set a private random JWT secret of at least 32 characters. Never use the example value for shared environments.
3. Configure SMTP to enable email verification and login MFA:
   - `MAIL_HOST`, `MAIL_PORT` (usually 587), `MAIL_USERNAME`, `MAIL_PASSWORD`
4. Optionally configure CAPTCHA with `CAPTCHA_ENABLED=true` and `CAPTCHA_SECRET` for Cloudflare Turnstile. When disabled, CAPTCHA is intentionally bypassed for local-only development; enable it when testing the real flow.
5. Run `mvn test`, then `mvn spring-boot:run`. SQLite creates `hotel.db` in the working directory.

JWT: `JWT_SECRET` must be at least 32 characters. There is no insecure fallback: the app will fail fast until `JWT_SECRET` is set. Tokens expire after 60 minutes. Email verification is required at registration, and login requires password + CAPTCHA (if enabled) + email OTP + JWT.

## Authentication flow

- `POST /api/auth/register` — `{ "name": "Guest", "email": "guest@example.com", "password": "long-password", "captchaToken": "..." }`
- `POST /api/auth/verify-email` — `{ "email": "guest@example.com", "code": "123456" }`
- `POST /api/auth/login` — `{ "email": "guest@example.com", "password": "...", "captchaToken": "..." }`; sends login OTP and returns `mfaRequired`.
- `POST /api/auth/verify-login` — same email + six-digit code; returns a Bearer JWT.
- `POST /api/auth/resend-otp` — requests a verification code (CAPTCHA checked when enabled).
- Send `Authorization: Bearer <token>` to protected endpoints. Registration never accepts a role, so users cannot self-register as managers. For this demo, promote a trusted user by updating the `role` field in the local SQLite database to `MANAGER` after registration and email verification.

## Main endpoints

### Customer/public
- `GET /api/rooms`, `GET /api/rooms/{id}`, `GET /api/rooms/availability?checkIn=YYYY-MM-DD&checkOut=YYYY-MM-DD`
- `POST /api/reservations`, `GET /api/reservations/mine`, `PATCH /api/reservations/{id}/cancel`
- `POST /api/upgrades`, `GET /api/upgrades/mine`
- `POST /api/chatbot` — deterministic FAQ chatbot; it does not call an external AI service.

### Manager (JWT role MANAGER)
- Rooms: `POST /api/rooms`, `PUT /api/rooms/{id}`, `DELETE /api/rooms/{id}`, `PATCH /api/rooms/{id}/availability?available=true`
- Customers: `GET /api/customers`, `GET /api/customers/{id}`, `PUT /api/customers/{id}`
- Reservations: `GET /api/reservations`, `PATCH /api/reservations/{id}/approve`, `/reject`, `/status?status=CONFIRMED`, `/cancel`, `/cancellation/approve`, `/cancellation/reject`
- Upgrades: `GET /api/upgrades/pending`, `PATCH /api/upgrades/{id}/approve`, `/reject`
- Reports: `GET /api/reports/summary`, `/revenue`, `/reservations/export`, `/rooms/export` (CSV downloads)

## Security and known demo limitations

- Passwords are BCrypt hashes; user roles are enforced by Spring Security method authorization.
- JWTs are signed and expire; email OTPs are random six-digit values with expiry and bounded attempts. Login OTPs are separate from account-verification OTPs.
- CAPTCHA uses server-side Turnstile verification when enabled. Keep the secret server-side.
- CORS is restricted to `FRONTEND_ORIGIN`; change it for your local frontend.
- OTPs and attempt counters are in-memory. Restarting the app clears them; this is acceptable for a single-process local demo but not multi-instance/production use. Add persistent OTP storage and rate limiting before exposure to a network.
- SQLite/JPA concurrency cannot fully prevent simultaneous double-booking without stronger transactional locking/constraints. This project is intended for local learning/testing.
- Manager provisioning is deliberately manual; do not add a public manager-registration endpoint.
- CSV export escapes quotes and neutralizes common spreadsheet formula prefixes.

## Error behavior

Validation errors return HTTP 400; access-denied requests return HTTP 403; malformed/expired JWTs are treated as unauthenticated and protected routes return HTTP 401. Business-rule failures generally return HTTP 400. Email service failures return HTTP 503 with a safe configuration hint. Always configure SMTP before testing registration/login end-to-end.
