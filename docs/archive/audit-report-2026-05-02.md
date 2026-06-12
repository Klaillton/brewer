# Brewer – Audit Report 2026-05-02

> **Scope:** Repo-wide ("C" / aggressive) audit of security vulnerabilities, performance bottlenecks,
> operational fragility, and maintainability risks.
>
> **Branch:** `copilot/audit-security-performance-maintainability`
> **Audited revision:** `master @ 866209ad`

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Risk Register](#2-risk-register)
3. [Detailed Findings](#3-detailed-findings)
4. [Remediations Implemented in This PR](#4-remediations-implemented-in-this-pr)
5. [Prioritised Remediation Roadmap (Follow-up)](#5-prioritised-remediation-roadmap-follow-up)
6. [Migration Notes](#6-migration-notes)

---

## 1. Executive Summary

**Brewer** is a Spring Boot 3.2 / Angular brewery-management system used primarily as an educational project (AlgaWorks). The codebase is reasonably well-structured, has existing security hardening in `SecurityConfig.java`, and uses Flyway for schema management. However, a number of operational, security, and maintainability issues were identified that require attention before any internet-facing or multi-user production deployment.

**Key risk areas (in descending severity):**

| Area | Severity |
|---|---|
| Health probe endpoints pointing to `/login` instead of Actuator | High |
| Docker Compose requires an external network that may not exist | Medium |
| Hardcoded AWS S3 bucket name and region in source code | Medium |
| File upload lacks server-side content-type validation | Medium |
| Legacy IE8 CDN scripts (html5shiv / respond.js) from external CDN | Low |
| `System.out.println` in production S3 storage code | Low |
| Unhandled error path in async file-upload thread | Low |
| AWS SDK v1 (`aws-java-sdk-s3`) still in use | Low (tech debt) |
| Synchronous PDF generation on request threads | Low (perf risk) |

All **High** and most **Medium/Low** issues were remediated directly in this PR (see §4).

---

## 2. Risk Register

| # | Title | Area | Severity | Likelihood | Exploitability / Impact | Fixed in PR? |
|---|---|---|---|---|---|---|
| R-01 | Health probes hit `/login` → weak liveness signal | Ops | High | High | Ops: pod stays "healthy" while DB/migrations are broken | ✅ Yes |
| R-02 | Docker Compose `external: true` network fails on fresh install | Ops | Medium | High | Dev: `docker compose up` fails with no clear error | ✅ Yes |
| R-03 | Hardcoded S3 bucket + URL in `FotoStorageS3.java` | Security/Ops | Medium | High | Ops: breaks if bucket changes; config management anti-pattern | ✅ Yes |
| R-04 | No server-side content-type validation on file upload | Security | Medium | Medium | Security: non-image files (SVG with JS, HTML, etc.) could be stored/served | ✅ Yes |
| R-05 | IE8 CDN scripts (MaxCDN) in layout templates | Security | Low | Low | Supply chain: external script injection if CDN is compromised | ✅ Yes |
| R-06 | `System.out.println` in S3 storage (prod logs) | Quality | Low | Certain | Ops: noisy logs, sensitive info could leak to stdout | ✅ Yes |
| R-07 | `FotoStorageRunnable` never completes on error | Reliability | Low | Medium | Ops: DeferredResult hangs ⇒ open connection until timeout | ✅ Yes |
| R-08 | Hardcoded AWS region `US_EAST_1` | Ops | Low | Medium | Ops: breaks deployments in other regions | ✅ Yes |
| R-09 | `StringUtils.isEmpty` deprecated in Spring 5.3+ | Quality | Low | Certain | Code quality | ✅ Yes |
| R-10 | AWS SDK v1 (`aws-java-sdk-s3 1.12.x`) | Tech debt | Low | Low | Maintenance: v1 is in maintenance mode; v2 performs better | ⬛ Follow-up |
| R-11 | Synchronous PDF generation on request thread | Performance | Medium | Medium | Perf: thread-blocking; long reports can exhaust server threads | ⬛ Follow-up |
| R-12 | `commons-beanutils` with potential historical CVE exposure | Security | Low | Low | Security: historically sensitive lib; version 1.11.0 is patched | ⬛ Monitor |
| R-13 | No brute-force / rate-limiting on `/login` | Security | Medium | High | Security: credential stuffing / password spray attacks | ⬛ Infra-level |
| R-14 | `unsafe-inline` in `script-src` CSP directive | Security | Medium | Medium | Security: weakens XSS protection (CSP bypass via inline scripts) | ⬛ Follow-up |
| R-15 | N+1 query risk in JPA associations | Performance | Medium | High | Perf: list pages may trigger N+1 SELECTs per row | ⬛ Follow-up |
| R-16 | Unmanaged thread creation in `FotosController` | Quality | Low | Low | Ops: thread pool unbounded; prefer managed `TaskExecutor` | ⬛ Follow-up |
| R-17 | CORS restricted to `localhost:*` (not production-safe) | Config | Low | Medium | Ops: Angular SPA on a different origin will be blocked | ⬛ Follow-up |

---

## 3. Detailed Findings

### 3.1 R-01 – Health probes use `/login` instead of Actuator (`High`)

**Files:** `Dockerfile:41-42`, `docker-compose.yml:58-63`, `k8s/app-deployment.yaml:83-105`

```yaml
# docker-compose.yml (before)
healthcheck:
  test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:8080/login"]
```

```dockerfile
# Dockerfile (before)
# Usa /login pois Actuator não está configurado
HEALTHCHECK ... CMD wget ... http://localhost:8080/login || exit 1
```

**Impact:** A probe against `/login` returns HTTP 200 even if Flyway migrations failed, the database is unreachable, or JPA is broken. The pod/container appears healthy while the application is functionally unavailable. Spring Boot Actuator is already on the classpath and `management.endpoints.web.exposure.include=health,info` is set in `application-docker.properties`.

**Fix:** Updated all three probe definitions to `/actuator/health` (Docker/Compose) and `/actuator/health/liveness` + `/actuator/health/readiness` (K8s) and enabled Spring Boot health probes.

---

### 3.2 R-02 – Docker Compose `external: true` network (`Medium`)

**File:** `docker-compose.yml:68-71`

```yaml
# Before
networks:
  default:
    name: observability-shared
    external: true
```

**Impact:** `docker compose up` fails on a fresh machine with:
```
network observability-shared declared as external, but could not be found
```
This breaks local development and CI environments without prior setup steps.

**Fix:** Changed the default network to an internal named network (`brewer`). Added `docker-compose.observability.yml` as an opt-in overlay for users who want to connect to a shared observability stack (Prometheus/Grafana/OTel). Usage:
```bash
# Standard (standalone):
docker compose up

# With observability stack:
docker network create observability-shared  # once
docker compose -f docker-compose.yml -f docker-compose.observability.yml up
```

---

### 3.3 R-03 – Hardcoded S3 bucket + URL (`Medium`)

**File:** `src/main/java/com/algaworks/brewer/storage/s3/FotoStorageS3.java:33-34,86-91`

```java
// Before
private static final String BUCKET = "aw-brewer-klaillton";
// ...
return "https://s3-sa-east-1.amazonaws.com/aw-brewer-klaillton/" + foto;
```

**Impact:**
- The bucket name is a hardcoded string belonging to a specific AWS account. Any fork or deployment attempt will fail unless this string is changed in source code.
- The URL uses a deprecated regional endpoint (`s3-sa-east-1.amazonaws.com`) and will be wrong if the bucket is in a different region.
- Violates the 12-factor app principle of externalising configuration.

**Fix:** Replaced `BUCKET` constant with `@Value("${aws.s3.bucket}")` injection. Replaced the hardcoded URL with `amazonS3.getUrl(bucket, foto).toString()` which uses the SDK-resolved URL. Added `aws.s3.region` property (default: `us-east-1`) to `S3Config.java`.

---

### 3.4 R-04 – No content-type validation on file uploads (`Medium`)

**File:** `src/main/java/com/algaworks/brewer/controller/FotosController.java`

```java
// Before – no content-type check
public DeferredResult<FotoDTO> upload(@RequestParam("files[]") MultipartFile[] files){
    DeferredResult<FotoDTO> resultado = new DeferredResult<>();
    Thread thread = new Thread(new FotoStorageRunnable(files, resultado, fotoStorage));
    thread.start();
    return resultado;
}
```

**Impact:** Any file type can be uploaded – scripts, HTML, SVG-with-JS, executables, etc. If files are later served via `/fotos/{nome}`, the server may serve malicious content with the wrong MIME type or be used for file-based attacks.

**Fix:** Added a server-side whitelist of permitted MIME types (`image/jpeg`, `image/png`, `image/gif`, `image/webp`) in `FotosController`. Requests with other types are rejected immediately with a descriptive error before any storage operation.

> **Follow-up:** The `Content-Type` header sent by the browser can be spoofed. A more robust check would read the file's magic bytes (Apache Tika is an option) to verify the actual format.

---

### 3.5 R-05 – IE8 CDN scripts from MaxCDN (`Low`)

**Files:** `src/main/resources/templates/layout/LayoutSimples.html:22-27`, `LayoutPadrao.html:21-26`

```html
<!--[if lt IE 9]>
    <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
    <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
<![endif]-->
```

**Impact:** IE8 is dead (2009, support ended 2016). Loading scripts from `oss.maxcdn.com` violates a strict CSP and is a supply-chain risk. If MaxCDN is compromised or redirected, the script executes in every visitor's browser.

**Fix:** Removed both IE8 conditional comment blocks from both layout templates.

---

### 3.6 R-06 – `System.out.println` in production S3 storage (`Low`)

**File:** `src/main/java/com/algaworks/brewer/storage/s3/FotoStorageS3.java:53`

```java
System.out.println("Enviou foto e thumbnail!");
```

**Impact:** Bypasses the application's structured logging (Log4j2/SLF4J). Pollutes stdout in production containers. Could be a concern if sensitive metadata is ever added to similar print statements.

**Fix:** Replaced with `logger.info("Foto e thumbnail enviados para o S3: {}", novoNome)`.

---

### 3.7 R-07 – Async upload thread never completes on error (`Low`)

**File:** `src/main/java/com/algaworks/brewer/storage/FotoStorageRunnable.java`

```java
// Before – no exception handling
public void run() {
    String nomeFoto = this.fotoStorage.salvar(files);
    String contentType = files[0].getContentType();
    resultado.setResult(new FotoDTO(nomeFoto, contentType, fotoStorage.getUrl(nomeFoto)));
}
```

**Impact:** If `fotoStorage.salvar()` throws (disk full, S3 error, etc.), the `DeferredResult` is never completed. The HTTP response waits until the default async timeout (no-timeout by default in Spring MVC), holding a Tomcat connector thread open indefinitely.

**Fix:** Wrapped the entire run body in try/catch; exceptions call `resultado.setErrorResult(e)` so the response is always completed.

---

### 3.8 R-08 – Hardcoded AWS region (`Low`)

**File:** `src/main/java/com/algaworks/brewer/config/S3Config.java:29,34`

```java
.withRegion(Regions.US_EAST_1)
```

**Impact:** Deployments in any non-`us-east-1` AWS region will either fail or silently connect to the wrong region.

**Fix:** Added `@Value("${aws.s3.region:us-east-1}")` property; the bean now constructs the region from configuration.

---

### 3.9 R-10 – AWS SDK v1 (follow-up)

**File:** `pom.xml:240-244`

```xml
<dependency>
    <groupId>com.amazonaws</groupId>
    <artifactId>aws-java-sdk-s3</artifactId>
    <version>1.12.797</version>
</dependency>
```

AWS SDK v1 is in [maintenance mode](https://aws.amazon.com/blogs/developer/the-aws-sdk-for-java-2-x-is-now-generally-available()). AWS SDK v2 offers non-blocking I/O, a more idiomatic API, better performance, and ongoing security updates.

**Recommendation:** Migrate to `software.amazon.awssdk:s3` (v2). The migration is a breaking API change. See the [migration guide](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/migration.html). This should be done as a dedicated PR.

---

### 3.10 R-11 – Synchronous PDF generation on request threads (follow-up)

**File:** `src/main/java/com/algaworks/brewer/controller/RelatoriosController.java:36-46`  
**Service:** `src/main/java/com/algaworks/brewer/Service/RelatorioHtmlPdfService.java`

```java
@PostMapping("/vendasEmitidas")
public Object gerarRelatorioVendasEmitidas(...) throws Exception {
    byte[] relatorio = relatorioHtmlPdfService.gerarRelatorioVendasEmitidas(periodoRelatorio);
    // ...
}
```

OpenHTMLtoPDF is CPU-intensive. A large date range can return thousands of rows and generate megabyte-sized PDFs. Under concurrent load, all Tomcat threads may be blocked on PDF rendering.

**Recommendations (follow-up):**
1. Add a `@PreAuthorize` or service-level limit on the date range (e.g., max 90 days).
2. Move to an async report generation job: the POST returns a `jobId`, a second GET endpoint polls for completion and streams the result.
3. Add a bounded `ExecutorService` for report jobs to prevent memory exhaustion.
4. Add a Micrometer timer metric to track report generation latency.

---

### 3.11 R-13 – No brute-force / rate-limiting on `/login` (follow-up)

**File:** `src/main/java/com/algaworks/brewer/config/SecurityConfig.java`

Spring Security doesn't provide rate-limiting out of the box. The login endpoint accepts unlimited authentication attempts.

**Recommendations:**
- **Short-term (infra):** Configure an Nginx/Traefik rate-limit rule for `POST /login` (e.g., 5 req/min per IP).
- **Medium-term (app):** Implement account lockout using Spring Security's `AbstractAuthenticationProcessingFilter` or a custom `AuthenticationEventPublisher` that increments a per-user attempt counter in a `ConcurrentHashMap` / Redis and throws `LockedException` after N failures.
- **Long-term:** Consider CAPTCHA (reCAPTCHA v3) on the login form after repeated failures.

---

### 3.12 R-14 – `unsafe-inline` in CSP `script-src` (follow-up)

**File:** `src/main/java/com/algaworks/brewer/config/SecurityConfig.java:79-85`

```java
"script-src 'self' 'unsafe-inline'; "
```

`'unsafe-inline'` allows arbitrary inline `<script>` tags, substantially weakening XSS protection.

**Root cause:** Thymeleaf templates use inline event handlers (e.g., `onclick="..."`) and inline `<script>` blocks. A clean fix requires either:
- Replacing inline scripts with external `.js` files (no CSP change needed)
- Using a nonce-based CSP (`script-src 'self' 'nonce-{random}'`) via `ContentSecurityPolicyHeaderWriter` + Thymeleaf nonce injection
- Using a hash-based CSP (`script-src 'self' 'sha256-...'`) for each inline script

This is a larger frontend refactor and should be done as a separate PR.

---

### 3.13 R-15 – N+1 query risk in JPA associations (follow-up)

**Likely affected files:** `src/main/java/com/algaworks/brewer/model/` (entity relationships), repository helper classes in `repository/helper/`

JPA `@ManyToOne` / `@OneToMany` relationships default to `EAGER` or `LAZY` loading. Without explicit `JOIN FETCH` in queries, list endpoints that return collections of `Venda` (with `Itens`, `Cliente`, `Cerveja`) may fire one SQL per row.

**Recommendations:**
- Enable `spring.jpa.show-sql=true` + `format_sql=true` in a dev session and count queries on the `/vendas` and `/cervejas` list pages.
- Add `@EntityGraph` or JPQL `JOIN FETCH` to avoid N+1.
- Consider adding `spring-data-jpa` `@QueryHints` with `jakarta.persistence.fetchgraph`.
- Check that database indices exist on common filter columns (`venda.status`, `cerveja.nome`, `cliente.nome`) – review Flyway migrations V01-V16.

---

## 4. Remediations Implemented in This PR

The following changes were made and are included in this PR:

| Fix | Files Changed |
|---|---|
| Health probes → `/actuator/health` | `Dockerfile`, `docker-compose.yml`, `k8s/app-deployment.yaml` |
| Enable Spring Boot health probes | `application.properties`, `application-docker.properties` |
| Permit `/actuator/health/**` in security filter | `SecurityConfig.java` |
| Docker Compose network: remove `external: true`; add `docker-compose.observability.yml` | `docker-compose.yml`, `docker-compose.observability.yml` (new) |
| S3 bucket + region from config (no more hardcoding) | `FotoStorageS3.java`, `S3Config.java`, `application.properties`, `application-docker.properties` |
| S3 URL built from SDK, not hardcoded string | `FotoStorageS3.java` |
| Replace `System.out.println` with SLF4J logger | `FotoStorageS3.java` |
| `StringUtils.isEmpty` → `StringUtils.hasText` (non-deprecated) | `FotoStorageS3.java` |
| `FotoStorageRunnable` error handling (DeferredResult always completed) | `FotoStorageRunnable.java` |
| Server-side content-type whitelist for file uploads | `FotosController.java` |
| Remove IE8 CDN scripts (html5shiv, respond.js) | `LayoutSimples.html`, `LayoutPadrao.html` |

---

## 5. Prioritised Remediation Roadmap (Follow-up)

### Priority 1 – Within 1 sprint

| # | Task | Effort |
|---|---|---|
| P1-01 | Implement login rate-limiting via Nginx/Traefik (or app-level) | S |
| P1-02 | Add file content inspection via magic bytes (Apache Tika) for upload validation | M |
| P1-03 | Add report date-range limit (max 90 days) to `RelatoriosController` | S |
| P1-04 | Fix `RuntimeRegressionTest` to use Testcontainers (currently fails without live MariaDB) | M |

### Priority 2 – Within 2 sprints

| # | Task | Effort |
|---|---|---|
| P2-01 | Migrate AWS SDK v1 → v2 | L |
| P2-02 | Async report generation (POST returns jobId, GET polls for file) | L |
| P2-03 | Replace `unsafe-inline` in CSP with nonce-based approach | M |
| P2-04 | Investigate and fix N+1 query risk on list endpoints | M |

### Priority 3 – Backlog

| # | Task | Effort |
|---|---|---|
| P3-01 | Replace unmanaged `new Thread(...)` in `FotosController` with `TaskExecutor` bean | S |
| P3-02 | Make CORS allowed-origins configurable via property (`app.cors.allowed-origins`) | S |
| P3-03 | Remove `commons-beanutils` if usage can be replaced by BeanUtils in Spring Framework | M |
| P3-04 | Add Micrometer metrics for PDF generation latency and file upload latency | S |
| P3-05 | Enable HSTS (`Strict-Transport-Security`) header in `SecurityConfig` | S |
| P3-06 | Evaluate `Permissions-Policy` header completeness | S |

---

## 6. Migration Notes

### Docker Compose network change

**Before:** `docker compose up` required the `observability-shared` external network to be pre-created.

**After:** `docker compose up` works standalone with no prior setup. The default network is now named `brewer` and is created automatically.

**To re-enable observability integration:**
```bash
# Create the shared network once
docker network create observability-shared

# Start with observability overlay
docker compose -f docker-compose.yml -f docker-compose.observability.yml up
```

### AWS S3 configuration

**Before:** Bucket name `aw-brewer-klaillton` was hardcoded in `FotoStorageS3.java`.

**After:** Bucket name is read from `aws.s3.bucket` property (env var `AWS_S3_BUCKET`). The `.env.example` file already has this variable – ensure it is set in your `.env` file or environment before starting.

```env
AWS_S3_BUCKET=your-bucket-name
AWS_S3_REGION=sa-east-1   # optional, defaults to us-east-1
```

### Health probe endpoints

Kubernetes liveness/readiness probes and Docker healthchecks now hit:
- `/actuator/health/liveness` and `/actuator/health/readiness`

See the updated deployment files for details.

---

**End of Report**

> This document was an audit snapshot. For current state, check the actual code and recent PRs.
