## AGENT.md — Guidance for AI Agents (Copilot, Claude, etc.)

This file tells agents how to work efficiently in this repository and avoid wasting context/tokens.

### Core Principles

- Be pragmatic and focused on code, manifests, and actual implementation.
- Never read large human-oriented docs, plans, or analysis reports unless the user explicitly says "read the [file]".
- Prefer small, targeted changes over broad refactors unless asked.
- When in doubt, ask for clarification instead of guessing from old docs.

### What to Ignore by Default (Context Hygiene)

**Strongly exclude from context unless explicitly requested:**

- `docs/archive/` (all historical analysis, plans, audits, decommissioning docs)
- Any file containing: decommissioning, audit, remediation, modernization, continuation, plan, report, coverage-plan, strategy, help, Grok, spike, temporary
- `frontend/package-lock.json` (use only `package.json` + `pom.xml` for dependencies)
- `legacy/`
- `src/main/resources/static/layout/vendors/` (old static assets)
- Most Grafana dashboard JSONs (`k8s/observability/dashboards/*.json`) — only read if the task is specifically about editing dashboards
- Large README.md / SECURITY.md unless onboarding or high-level architecture is needed

**Good to read by default:**

- Source code (Java, TypeScript, HTML templates)
- Kubernetes manifests under `k8s/`
- `deploy.sh`, `pom.xml`, `package.json`, Dockerfiles
- `.github/instructions/master.instructions.md`
- `.claude/skills/` (when doing E2E/Playwright work)
- Config files: `application*.properties`, `config/*.yaml` (prometheus, loki, tempo, otel)
- Current `.copilotignore` and `.claudeignore`

### Repo-Specific Notes

**Brewer**
- Primary deployment path on devserverpi is Kubernetes only (`deploy.sh` + `k8s/` + kustomize).
- Docker Compose files are legacy / local-dev only.
- Frontend is Angular 21 + Playwright for E2E.
- Use the multi-persona flow defined in `master.instructions.md` when the task is complex.

**Observability (related stack)**
- Source of truth for the observability platform is `k8s/observability/`.
- `docker-compose.yml` is for local development only.
- Prefer editing the k8s manifests and config/*.yaml over compose.

**devstack-infra**
- Used to orchestrate deploys to the dev Pi via repository_dispatch events.
- Keep it minimal.

### Workflow Reminders

- Start by identifying the right persona(s) from master.instructions.md when the user says "@Master" or similar.
- For implementation tasks: @JavaSenior or @Pair first, then @CodeReviewer.
- For infra/observability: @DevOps.
- Always check current k8s manifests and deploy scripts before suggesting compose-based solutions.

### When to Ask the User

- Before reading any file in docs/ (except if it's clearly a code-related config).
- Before touching legacy/ or old static assets.
- If the task seems to require broad historical context (audit, old plans).

This file is meant to be read early by agents. Keep it updated as the project evolves.
