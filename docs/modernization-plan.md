# Plano de Modernização — MVC acoplado para Backend/Frontend desacoplado

## 1) Objetivo

Transformar o Brewer para arquitetura desacoplada (**Spring Boot + Angular**), com operação **multi-cloud**, adicionando:
- gerenciamento de perfil do usuário;
- reset/recuperação de senha;
- cadastro externo com CAPTCHA;
- ativação de conta por e-mail;
- estratégia de testes com meta de cobertura de **98% como objetivo aspiracional**.

## 2) Estado atual (AS-IS)

- Backend Spring Boot com MVC + Thymeleaf e APIs REST coexistindo.
- Frontend Angular já existe, porém o projeto ainda mantém fluxo integrado server-side.
- Build backend configurado para **Java 25** (`pom.xml`).
- Testes já têm plano evolutivo em `docs/test-coverage-plan.md`.

## 3) Requisitos levantados

### 3.1 Funcionais
- Expor funcionalidades de negócio por APIs versionadas (`/api/v1`), sem dependência de renderização server-side.
- Implementar **Perfil do Usuário**:
  - visualizar/editar dados básicos;
  - troca de senha autenticada;
  - atualização de preferências básicas (extensível).
- Implementar **Recuperação/Reset de senha**:
  - solicitar recuperação por e-mail;
  - token único com expiração;
  - redefinição de senha com invalidação de token.
- Implementar **Cadastro externo**:
  - formulário público;
  - validação anti-bot com CAPTCHA;
  - ativação de conta via link por e-mail.

### 3.2 Não funcionais
- Stack alvo: **Angular + Spring Boot (Java 25)**.
- Desacoplamento por contrato de API (OpenAPI).
- Estratégia **multi-cloud** com portabilidade (evitar lock-in).
- Segurança: OWASP ASVS baseline, proteção a abuso de endpoints públicos.
- Observabilidade: logs estruturados, métricas e tracing.

## 4) Stack sugerida (TO-BE)

- **Frontend**: Angular (atual), Angular Material, RxJS, testes com Jest/Karma + Playwright E2E.
- **Backend**: Spring Boot 3.5+, Java 25, Spring Security, Spring Data JPA, Flyway.
- **Autenticação**: JWT access + refresh token (ou sessão segura se houver exigência de compatibilidade), BCrypt/Argon2.
- **Captcha**: Cloudflare Turnstile (preferencial) ou Google reCAPTCHA v3 via feature toggle.
- **E-mail transacional**: provider SMTP/API desacoplado por interface (SendGrid/SES/Mailgun plugável).
- **Infra multi-cloud**:
  - containers Docker;
  - Kubernetes + Helm/Kustomize;
  - banco gerenciado com estratégia de backup/restore portável;
  - storage S3-compatible (AWS S3, GCS interop, MinIO em dev).
- **Observabilidade**: OpenTelemetry + Prometheus/Grafana + logs centralizados.
- **CI/CD**: GitHub Actions com gates de testes, segurança e cobertura.

## 5) Planejamento de execução por fases

### Fase 0 — Descoberta e contratos (1 sprint)
- Mapear jornadas atuais MVC e SPA.
- Publicar contrato OpenAPI inicial dos recursos já existentes.
- Definir NFRs (latência, disponibilidade, segurança, cobertura).

### Fase 1 — Base de desacoplamento (1–2 sprints)
- Versionar APIs (`/api/v1`) e padronizar erros (problem+json).
- Criar camada anti-corrupção para não quebrar Thymeleaf no curto prazo.
- Habilitar CORS, rate-limit e política de segurança para APIs públicas.

### Fase 2 — Identidade e conta (2 sprints)
- Entregar endpoints de perfil do usuário.
- Entregar fluxo de reset/recuperação de senha com token e auditoria.
- Entregar cadastro externo + CAPTCHA + ativação por e-mail.
- Cobrir fluxos com testes unitários, integração e E2E.

### Fase 3 — Frontend Angular desacoplado (2 sprints)
- Migrar telas críticas para consumo exclusivo de API.
- Implementar guards/interceptors para autenticação/autorização.
- Remover dependências de páginas server-side dos fluxos migrados.

### Fase 4 — Multi-cloud e operação (1–2 sprints)
- Consolidar manifests/Helm para deploy em múltiplos provedores.
- Externalizar configs e segredos por ambiente.
- Implementar estratégia de deploy progressivo (blue/green ou canary).

### Fase 5 — Descomissionamento MVC integrado (1 sprint)
- Desativar rotas/views Thymeleaf remanescentes.
- Atualizar documentação operacional e de suporte.

## 6) Plano de testes e validação (meta 98%)

- **Pirâmide de testes**:
  - unitários (regras de negócio e segurança);
  - integração (repositórios, autenticação, e-mail/captcha mockados);
  - contrato (OpenAPI);
  - E2E (Playwright) para jornadas ponta a ponta.
- **Cobertura**:
  - meta aspiracional (quando viável): **98%**;
  - gate inicial recomendado: **90% em módulos novos/alterados**, com evolução progressiva para **95%** e depois **98%** nos fluxos críticos.
- **Qualidade/Security gates no CI**:
  - testes backend + frontend;
  - cobertura (JaCoCo/Angular coverage);
  - SAST (CodeQL), dependency-check e secret scanning;
  - smoke test pós-deploy.

## 7) Critérios de aceite

- Frontend Angular opera sem dependência de renderização Thymeleaf.
- Fluxos de perfil, recuperação/reset e cadastro externo com ativação por e-mail disponíveis e testados.
- Deploy reproduzível em pelo menos dois provedores cloud (ou ambientes equivalentes).
- Métricas e logs permitindo diagnóstico de ponta a ponta.
- Cobertura conforme estratégia acordada e gates ativos no pipeline.

## 8) Riscos e mitigação

- **Risco**: meta de 98% global inviável no curto prazo.  
  **Mitigação**: gate de 98% em código novo/alterado + plano incremental no legado.
- **Risco**: lock-in de serviços cloud.  
  **Mitigação**: abstrações por interface + padrões portáveis (K8s, S3-compatible, SMTP/API).
- **Risco**: regressão durante migração híbrida MVC+SPA.  
  **Mitigação**: rollout por feature flags e testes E2E por jornada.
