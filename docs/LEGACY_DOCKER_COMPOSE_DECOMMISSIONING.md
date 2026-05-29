# Legacy Docker Compose Decommissioning Plan

**Date**: 2026-05-29  
**Author**: Grok (analysis performed on devserverpi)  
**Status**: Draft for review  
**Related Projects**: `brewer`, `observability-epo`

---

## 1. Executive Summary

Both the `brewer` and `observability-epo` projects are currently in a **dangerous hybrid state**:

- They have proper Kubernetes manifests and deployment processes (`k8s/` folders + `deploy.sh`).
- They still contain the original Docker Compose files that deploy almost identical stacks.

This duplication has been the **direct cause** of extreme resource consumption on `devserverpi`, leading to high load averages (35–45+), severe memory pressure, and thermal throttling/overheating events.

**Goal**: Completely retire Docker Compose usage on `devserverpi` while preserving it (if desired) only for local developer machines.

---

## 2. Current State (as of 2026-05-29)

### Server Health (after emergency cleanup)

- Load average: ~4.7 (was 35–45 during the incident)
- Memory: 1.5 GiB free + 6.0 GiB available
- Temperature: 50.1°C (stable)
- Brewer application: Scaled to 0 replicas (`brewer` deployment)
- MariaDB: Running normally (1/1)
- Most heavy observability components (Loki, Tempo, Grafana, Prometheus, SonarQube) stopped

### What Was Running Duplicated

| Component              | Docker Compose                  | Kubernetes (k3s)              | Impact                  |
|------------------------|----------------------------------|-------------------------------|-------------------------|
| Brewer Application     | `brewer-app` container          | `brewer` deployment           | Very High CPU           |
| MariaDB                | `brewer-db`                     | `mariadb` deployment          | Medium                  |
| Full Observability     | Multiple containers             | Full stack in `observability` | Extremely High          |
| SonarQube              | Standalone                      | -                             | High                    |

---

## 3. Root Cause Analysis

The primary cause of the incident was **architectural drift** during the Docker → Kubernetes migration:

1. The migration to Kubernetes was started (good `k8s/` manifests exist).
2. The old Docker Compose files were **never removed or clearly marked as deprecated**.
3. People continued to (or scripts continued to) bring up services via `docker compose`.
4. On `devserverpi`, this resulted in the **same logical services** running twice.

A Raspberry Pi 4 (even 8GB model) cannot sustain:
- 2× Java applications
- 2× full observability stacks (Loki + Tempo + Prometheus + Grafana + OTEL)
- SonarQube
- k3s overhead

This is not a "Pi is weak" problem — it is a **process and repository hygiene** problem.

---

## 4. Projects Analysis

### 4.1 `brewer`

**Current correct path**:
- `deploy.sh` (official deployment script)
- `k8s/` folder with proper manifests (app, mariadb, ingress, network policies, cert-manager, etc.)
- Uses Kustomize (`kustomization.yaml`)

**Legacy files that must be retired**:
- `docker-compose.yml` (main — runs app + db + nginx)
- `docker-compose.observability.yml`
- `docker-compose.sonarqube.yml`
- `Dockerfile` (still needed for image build, but usage must be only via k8s)

**Observations**:
- `deploy.sh` already does the right thing (builds image + `kubectl apply -k`).
- There is still a `.env` and nginx configuration tied to the Docker Compose world.

### 4.2 `observability-epo`

**Current correct path**:
- `k8s/observability/` with full set of manifests (namespace, apps, ingress, storage, backup/restore, examples, etc.)

**Legacy files**:
- `docker-compose.yml` (very complete — defines Loki, Tempo, Prometheus, Grafana, OTEL Collector, etc.)

This project appears to be the **source of truth** for the observability platform. Having both Docker Compose and Kubernetes versions creates massive confusion.

---

## 5. Recommended Strategy

**Principle**: `devserverpi` must become a **Kubernetes-only environment**.

**Rules going forward**:

| Environment          | Allowed Deployment Method      | Docker Compose Allowed? |
|----------------------|--------------------------------|--------------------------|
| devserverpi          | Only Kubernetes (k3s)          | **No**                   |
| Developer laptops    | Docker Compose (for speed)     | Yes (local only)         |
| CI/CD                | Kubernetes manifests           | No                       |

---

## 6. Detailed Action Plan

### Phase 0 — Immediate Stabilization (Already partially done)

- [x] Emergency stop of heavy containers
- [x] Scale down `brewer` deployment to 0
- [ ] Full cleanup of any remaining legacy containers (see commands below)
- [ ] Validate that only MariaDB + essential k3s components are running

### Phase 1 — Documentation (This Document)

- Create this file in `docs/LEGACY_DOCKER_COMPOSE_DECOMMISSIONING.md` in **both** repositories.
- Create a short `DEPRECATED.md` or big warning header inside each legacy `docker-compose*.yml`.

### Phase 2 — Process & Communication

- Update main `README.md` in both projects with clear deployment instructions for `devserverpi`.
- Add a section: "Deployment on devserverpi (Kubernetes only)".
- Communicate to the team that running `docker compose up` on devserverpi is now forbidden.

### Phase 3 — Codebase Cleanup (Git)

**For `brewer`**:
- Move the three `docker-compose*.yml` files + related nginx config (if only used by compose) to `legacy/docker-compose/` or `archive/legacy-docker/`.
- Keep the `Dockerfile` (still needed to build the image for k8s).
- Consider moving `docker-compose.observability.yml` logic into proper Kubernetes Service discovery.

**For `observability-epo`**:
- Move `docker-compose.yml` to `legacy/docker-compose/`.
- Ensure all important configuration lives in `k8s/observability/`.

### Phase 4 — Validation on devserverpi

1. Bring brewer back with `./deploy.sh` (1 replica).
2. Monitor for 30–60 minutes:
   - Load average
   - Memory usage
   - `vcgencmd measure_temp`
   - `kubectl top pods -n brewer` (if metrics-server is working)
3. Gradually re-enable parts of the observability stack only via Kubernetes.

### Phase 5 — Guardrails (Recommended)

- Add a script `scripts/validate-deployment-environment.sh` that refuses to run if it detects it is being executed on `devserverpi` via Docker Compose.
- Add a pre-commit hook or CI check that warns when someone modifies the legacy compose files.
- Consider renaming the legacy compose files to `docker-compose.legacy.yml` with a big header.

---

## 7. Specific Recommendations per Project

### Brewer

**Keep**:
- `Dockerfile`
- `k8s/`
- `deploy.sh`
- `nginx/` (evaluate if still needed for k8s ingress)

**Retire / Archive**:
- All `docker-compose*.yml`
- Possibly parts of the root `.env` that were only for Compose

**Improvements**:
- Add resource requests/limits to the brewer deployment (critical on Pi 4).
- Add proper liveness/readiness probes (they seem partially present).

### Observability-epo

**Keep**:
- Entire `k8s/observability/` folder (this should become the only way to deploy the stack on any cluster).

**Retire / Archive**:
- `docker-compose.yml`

**Improvements**:
- The Kubernetes manifests look quite complete. Consider making this repo the central "Observability Platform" that other projects depend on (via Helm or Kustomize bases).

---

## 8. Proposed Scripts and Automation

Create the following in both repos under `scripts/`:

- `cleanup-legacy-docker.sh` — Stops and removes containers/networks/volumes created by the old compose files.
- `validate-no-docker-compose-on-server.sh` — Can be called from deploy scripts.

Example content for `cleanup-legacy-docker.sh` (safe version):

```bash
#!/usr/bin/env bash
set -euo pipefail

echo "Stopping legacy Docker Compose stacks for brewer + observability..."

# Brewer legacy stacks
docker compose -f docker-compose.yml down --remove-orphans 2>/dev/null || true
docker compose -f docker-compose.observability.yml down 2>/dev/null || true
docker compose -f docker-compose.sonarqube.yml down 2>/dev/null || true

# Observability-epo legacy stack
cd ../observability-epo
docker compose down --remove-orphans 2>/dev/null || true

echo "Legacy stacks stopped."
```

---

## 9. Risks and Mitigations

| Risk                                      | Likelihood | Impact | Mitigation |
|-------------------------------------------|------------|--------|----------|
| Someone runs `docker compose up` on devserverpi again | High | Very High | Strong documentation + guardrail script |
| Important volume data only exists in Docker Compose volumes | Medium | High | Document data migration or keep MariaDB volume |
| Team members are confused about the "correct" way to deploy | High | Medium | Excellent README + this document |
| Local development workflow breaks | Medium | Medium | Keep Docker Compose working for laptops |

---

## 10. Rollback Plan

If major issues are found after fully removing compose support:

1. Restore the compose files from Git history (they will still exist in previous commits).
2. Scale down the problematic Kubernetes deployments.
3. Bring up the legacy stack temporarily while fixing the k8s manifests.

---

## 11. Prioritized Task List

| Priority | Task | Repo | Owner | Status |
|----------|------|------|-------|--------|
| P0 | Commit this document to both repos | Both | - | This PR |
| P0 | Add DEPRECATED header to all legacy compose files | Both | - | - |
| P1 | Update main READMEs with clear "Kubernetes only on devserverpi" instructions | Both | - | - |
| P1 | Create `scripts/cleanup-legacy-docker.sh` | Both | - | - |
| P2 | Move legacy compose files to `legacy/` folder | Both | - | - |
| P2 | Add resource limits to brewer deployment | brewer | - | - |
| P3 | Add validation script that blocks compose usage on devserverpi | brewer | - | - |
| P3 | Review nginx configuration (k8s vs compose) | brewer | - | - |

---

## Appendix A — Useful Commands (Current Server State)

```bash
# Check overall health
uptime && free -h && vcgencmd measure_temp

# Brewer status
sudo k3s kubectl get all -n brewer

# Observability status
sudo k3s kubectl get all -n observability

# See what is still using significant resources
docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}"
```

---

**End of Document**

This file should be reviewed by the team and turned into actionable GitHub issues or a migration ticket.