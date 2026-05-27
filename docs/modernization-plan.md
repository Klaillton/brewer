# Plano de Arquitetura greenfield — referência para novo repositório

## 1) Objetivo

Definir um plano **para criação do zero em outro repositório**, com arquitetura desacoplada (**Spring Boot + Angular**) e operação **multi-cloud**, incluindo:
- gerenciamento de perfil do usuário;
- reset/recuperação de senha;
- cadastro externo com CAPTCHA;
- ativação de conta por e-mail;
- estratégia de testes com meta de cobertura de **98% como objetivo aspiracional**.

## 2) Contexto de origem (repositório atual)

- O repositório atual serve apenas como base de requisitos funcionais e regras de negócio.
- A implementação alvo deste documento deve nascer **do zero** em novo repositório (sem migração incremental de código legado).
- A stack alvo permanece **Angular + Spring Boot Java 25**, com APIs como contrato principal.

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

- **Frontend**: Angular, Angular Material, RxJS, **Jest como runner padrão** para testes unitários/componente, **Karma apenas para cenários legados/excepcionais** que dependam de infraestrutura específica; Playwright para E2E.
- **Backend**: Spring Boot 3.5+, Java 25, Spring Security, Spring Data JPA, Flyway.
- **Autenticação**: JWT access + refresh token (ou sessão segura se houver exigência de compatibilidade), BCrypt/Argon2.
- **Captcha**: Cloudflare Turnstile (preferencial) ou Google reCAPTCHA v3 via feature toggle.
- **E-mail transacional**: provider SMTP/API desacoplado por interface (SendGrid/SES/Mailgun plugável).
- **Infra multi-cloud**:
  - containers Docker;
  - Kubernetes + Helm/Kustomize;
  - banco gerenciado com estratégia de backup/restore portável;
  - storage com porta de abstração por interface: implementação nativa para **AWS S3**, implementação nativa para **GCS**, e **MinIO** em dev/testes locais.
- **Observabilidade**: OpenTelemetry + Prometheus/Grafana + logs centralizados.
- **CI/CD**: GitHub Actions com gates de testes, segurança e cobertura.

## 5) Planejamento de execução por fases (novo repositório)

### Fase 0 — Planejamento inicial e contratos (1 sprint)
- Levantar jornadas e regras de negócio no repositório de origem.
- Publicar contrato OpenAPI inicial dos recursos prioritários.
- Definir NFRs (latência, disponibilidade, segurança, cobertura).

### Fase 1 — Base backend/frontend (1–2 sprints)
- Criar estrutura base do novo monorepo/repositórios (backend + frontend + IaC).
- Versionar APIs (`/api/v1`) e padronizar erros (problem+json).
- Habilitar CORS, rate-limit e política de segurança para APIs públicas.

### Fase 2 — Identidade e conta (2 sprints)
- Entregar endpoints de perfil do usuário.
- Entregar fluxo de reset/recuperação de senha com token e auditoria.
- Entregar cadastro externo + CAPTCHA + ativação por e-mail.
- Cobrir fluxos com testes unitários, integração e E2E.

### Fase 3 — Frontend Angular (2 sprints)
- Implementar telas críticas com consumo exclusivo de API.
- Implementar guards/interceptors para autenticação/autorização.

### Fase 4 — Multi-cloud e operação (1–2 sprints)
- Consolidar manifests/Helm para deploy em múltiplos provedores.
- Externalizar configs e segredos por ambiente.
- Implementar estratégia de deploy progressivo (blue/green ou canary).

### Fase 5 — Transferência, endurecimento e entrada em produção (1 sprint)
- Consolidar documentação operacional e de suporte.
- Publicar runbooks e checklist de readiness para produção.
- Realizar transferência formal para o time do novo repositório.

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

- Frontend Angular opera sem dependência de renderização server-side.
- Fluxos de perfil, recuperação/reset e cadastro externo com ativação por e-mail disponíveis e testados.
- Deploy reproduzível em pelo menos dois provedores cloud (ou ambientes equivalentes).
- Métricas e logs permitindo diagnóstico de ponta a ponta.
- Cobertura conforme estratégia acordada e gates ativos no pipeline.

## 8) Riscos e mitigação

- **Risco**: meta de 98% global inviável no curto prazo.  
  **Mitigação**: gates progressivos aplicados ao **novo código do repositório greenfield** (90% → 95% → 98% nos fluxos críticos), sem dependência de migração incremental de código legado.
- **Risco**: lock-in de serviços cloud.  
  **Mitigação**: abstrações por interface + padrões portáveis (K8s, storage por adaptadores S3/GCS, SMTP/API).
- **Risco**: divergência de escopo entre repositório origem e novo repositório.  
  **Mitigação**: gestão por backlog rastreável (epic → história → critério de aceite) e handoff por marcos.

## 9) Entregáveis para transferência ao novo repositório

- Documento de visão e escopo (este plano).
- Backlog inicial por épicos (arquitetura, identidade, segurança, multi-cloud, qualidade).
- Contrato OpenAPI inicial versionado.
- Matriz de ambientes (dev/hml/prod) e variáveis.
- Critérios de pronto e critérios de aceite por fase.

## 10) Passos práticos recomendados para adoção (execução no novo repositório)

1. **Kickoff técnico (semana 1)**  
   Consolidar escopo funcional, NFRs e decisões arquiteturais; aprovar template de ADR e Definition of Done.
2. **Estrutura inicial (semana 1–2)**  
   Criar repositórios (backend/frontend/infra), convenções de branch, padrão de versionamento e pipelines mínimos (build + testes + segurança).
3. **Contrato primeiro (semana 2)**  
   Publicar OpenAPI v1 para perfil/reset/cadastro/ativação e gerar stubs cliente/servidor para reduzir retrabalho.
4. **MVP de identidade (semanas 3–6)**  
   Entregar backend + frontend dos fluxos críticos (perfil, reset, cadastro com CAPTCHA e ativação por e-mail) com testes automatizados.
5. **Endurecimento operacional (semanas 6–8)**  
   Fechar observabilidade, gestão de segredos, política de backup/restore e readiness para incidentes (runbooks).
6. **Multi-cloud e handoff (semanas 8–10)**  
   Validar deploy em dois provedores/ambientes equivalentes, registrar evidências de portabilidade e executar transferência formal para o time responsável.
