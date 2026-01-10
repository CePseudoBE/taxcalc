# Production Deployment Checklist - CalcTax

This checklist must be completed before deploying CalcTax to production.

---

## Pre-Deployment Verification

### Environment Variables

#### Backend (.env)

| Variable | Required | Description |
|----------|----------|-------------|
| `SPRING_DATASOURCE_URL` | Yes | PostgreSQL connection URL (e.g., `jdbc:postgresql://host:5432/backtaxes`) |
| `SPRING_DATASOURCE_USERNAME` | Yes | Database username |
| `SPRING_DATASOURCE_PASSWORD` | Yes | Database password (use strong, unique password) |
| `APP_SECURITY_ENABLED` | **CRITICAL** | Must be `true` in production |
| `APP_TOKEN_EXPIRATION_HOURS` | No | Token expiration (default: 24) |
| `APP_CORS_ALLOWED_ORIGINS` | Yes | Production domain(s) only (e.g., `https://calctax.be`) |
| `GOOGLE_CLIENT_ID` | Yes | Google OAuth client ID |
| `REDIS_HOST` | Yes (prod) | Redis host for distributed rate limiting |
| `REDIS_PORT` | No | Redis port (default: 6379) |
| `REDIS_PASSWORD` | No | Redis password if authentication enabled |

#### Frontend (.env)

| Variable | Required | Description |
|----------|----------|-------------|
| `NUXT_BACKEND_URL` | Yes | Backend API URL (internal, e.g., `http://backend:8080/api`) |
| `NUXT_SESSION_SECRET` | **CRITICAL** | 32+ character random string. App fails to start without it. |
| `NUXT_PUBLIC_GOOGLE_CLIENT_ID` | Yes | Same as backend's GOOGLE_CLIENT_ID |
| `NUXT_PUBLIC_SITE_URL` | Yes | Production URL (e.g., `https://calctax.be`) |
| `NODE_ENV` | Yes | Must be `production` |

---

## Security Checklist

### CRITICAL - Application Will Fail Without These

- [ ] `APP_SECURITY_ENABLED=true` is set in backend
- [ ] `NUXT_SESSION_SECRET` is set (32+ chars, cryptographically random)
  ```bash
  # Generate with:
  openssl rand -base64 32
  ```
- [ ] All database credentials use environment variables (not hardcoded)

### HIGH - Security Features Implemented (Dev)

These features are already implemented and will work automatically:

- [x] **Rate Limiting** - Backend: 10 req/min auth, 100 req/min general per IP (Redis in prod)
- [x] **HSTS Header** - 1 year with includeSubDomains and preload
- [x] **CSP Header** - Restricts resource loading
- [x] **Password Complexity** - 8+ chars, uppercase, lowercase, digit, special char
- [x] **Pagination Limits** - Max 100 items per page
- [x] **Security Headers** - X-Frame-Options, X-Content-Type-Options, etc.

### MEDIUM - Verify in Production

- [ ] CORS origins updated to production domain only
- [ ] SSL/TLS certificate is valid
- [ ] HTTPS is enforced (HTTP redirects to HTTPS)
- [ ] Database is not publicly accessible
- [ ] `.env` files are not in version control

---

## Infrastructure Checklist

### Database

- [ ] PostgreSQL is running and accessible
- [ ] Database user has minimal required permissions
- [ ] Connection pooling configured (HikariCP default is fine)
- [ ] Regular backups scheduled

### Reverse Proxy (Nginx/Traefik/etc.)

- [ ] SSL termination configured
- [ ] HTTP to HTTPS redirect
- [ ] Proxy headers forwarded (X-Forwarded-For, X-Real-IP)
- [ ] WebSocket support if needed

### Monitoring

- [ ] Health endpoint accessible (`/actuator/health`)
- [ ] Logging configured (consider log aggregation)
- [ ] Error tracking (Sentry, etc.) if desired

---

## Post-Deployment Verification

### Security Tests

```bash
# Check HSTS header
curl -I https://calctax.be | grep -i strict-transport

# Check CSP header
curl -I https://calctax.be | grep -i content-security-policy

# Check rate limiting (should get 429 after 10 rapid requests)
for i in {1..15}; do curl -s -o /dev/null -w "%{http_code}\n" https://calctax.be/api/auth/login; done
```

### Functional Tests

- [ ] User registration works
- [ ] User login works
- [ ] Google OAuth login works
- [ ] Tax calculation returns correct values
- [ ] All three regions (Wallonia, Flanders, Brussels) work

---

## Quick Reference

### Generate Secure Secrets

```bash
# Session secret
openssl rand -base64 32

# Database password
openssl rand -base64 24
```

### Docker Compose Example

```yaml
services:
  redis:
    image: redis:7-alpine
    command: redis-server --appendonly yes

  backend:
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/backtaxes
      - SPRING_DATASOURCE_USERNAME=backtaxes
      - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
      - APP_SECURITY_ENABLED=true
      - APP_CORS_ALLOWED_ORIGINS=https://calctax.be
      - GOOGLE_CLIENT_ID=${GOOGLE_CLIENT_ID}
      - REDIS_HOST=redis

  frontend:
    environment:
      - NODE_ENV=production
      - NUXT_BACKEND_URL=http://backend:8080/api
      - NUXT_SESSION_SECRET=${SESSION_SECRET}
      - NUXT_PUBLIC_GOOGLE_CLIENT_ID=${GOOGLE_CLIENT_ID}
      - NUXT_PUBLIC_SITE_URL=https://calctax.be
```

---

## Known Issues / Warnings

1. **CSP 'unsafe-inline'**: Required for Nuxt/Vue hydration. Consider using nonces in future.
2. **Frontend sessions**: Cookie-based (encrypted). Already works across multiple instances via shared `NUXT_SESSION_SECRET`.
3. **Backend rate limiting**: Uses Redis in production for distributed rate limiting. Falls back to in-memory in dev.

---

**Last Updated:** 2026-01-10
**Review Schedule:** Before each major deployment
