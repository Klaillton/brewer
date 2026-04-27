# Brewer — Sistema de Gerenciamento de Cervejaria

Sistema web para gestão de cervejaria: cadastro de cervejas, clientes, vendas, relatórios e controle de usuários. Desenvolvido com Spring Boot 3 (MVC + REST) e Angular, com suporte a deploy via Docker.

## Tecnologias

### Backend
| Tecnologia | Versão | Função |
|---|---|---|
| Java | 17 | Linguagem de programação |
| Spring Boot | 3.2.5 | Framework principal |
| Spring Security 6 | — | Autenticação e autorização (form login) |
| Spring Data JPA + Hibernate 6 | — | ORM e persistência |
| Thymeleaf | — | Templates server-side (views MVC) |
| Flyway | 10.10.0 | Migrations de banco de dados |
| MariaDB | 11.3 (prod) / H2 (dev) | Banco de dados |
| EhCache 3 | — | Cache de segundo nível |
| JasperReports / OpenHTMLtoPDF | — | Geração de relatórios PDF |
| Spring Mail | — | Envio de e-mails transacionais |

### Frontend (SPA)
| Tecnologia | Versão | Função |
|---|---|---|
| Angular | 21.2.7 | Framework SPA |
| TypeScript | 5.9 | Linguagem tipada |
| RxJS | — | Programação reativa |

### Infraestrutura
| Tecnologia | Versão | Função |
|---|---|---|
| Docker | — | Containerização |
| Docker Compose | — | Orquestração local |
| eclipse-temurin:17-jre | — | Imagem base de runtime (suporta ARM64) |

### Testes
| Ferramenta | Escopo |
|---|---|
| JUnit 5 | Unitários (backend) |
| Playwright | E2E (web + API) |

## Estrutura do Projeto

```
brewer/
├── Dockerfile                  # Imagem Docker multi-stage
├── docker-compose.yml          # Orquestração app + MariaDB
├── .env.example                # Template de variáveis de ambiente
├── Procfile                    # Deploy Heroku/Render
├── pom.xml                     # Build Maven
├── src/
│   └── main/
│       ├── java/com/algaworks/brewer/
│       │   ├── api/            # Controllers REST
│       │   ├── config/         # Configurações Spring (Security, Cache, Mail…)
│       │   ├── controller/     # Controllers MVC (Thymeleaf)
│       │   ├── model/          # Entidades JPA
│       │   ├── repository/     # Repositórios Spring Data
│       │   ├── service/        # Camada de serviços
│       │   ├── dto/            # DTOs de API
│       │   └── storage/        # Abstração de armazenamento (S3 / local)
│       └── resources/
│           ├── application.properties
│           ├── db/migration/   # Scripts Flyway (V01…V15)
│           ├── templates/      # Views Thymeleaf
│           └── relatorios/     # Templates JasperReports (.jasper)
├── frontend/                   # Aplicação Angular
│   └── src/app/
│       ├── components/         # Componentes
│       ├── models/             # Modelos TypeScript
│       └── services/           # Serviços HTTP
└── e2e/                        # Testes E2E Playwright
    └── tests/
```

## API REST

| Recurso | Endpoint base | Métodos disponíveis |
|---|---|---|
| Cervejas | `/api/cervejas` | GET, POST, PUT, DELETE |
| Estilos | `/api/estilos` | GET, POST, PUT, DELETE |
| Clientes | `/api/clientes` | GET, POST, PUT, DELETE |
| Cidades | `/api/cidades` | GET, POST, PUT |
| Estados | `/api/estados` | GET |
| Vendas | `/api/vendas` | GET |
| Estatísticas de vendas | `/api/vendas/stats/por-origem` | GET |
| Usuários | `/api/usuarios` | GET, POST, PUT |

## Executando com Docker (recomendado)

> Pré-requisito: Docker e Docker Compose instalados. Compatível com AMD64 e **ARM64 (Raspberry Pi)**.

```bash
# 1. Copie o arquivo de variáveis de ambiente
cp .env.example .env
# Edite .env se necessário (senhas, e-mail, S3)

# 2. Build e inicialização
docker compose up --build

# 3. Acesse a aplicação
# http://localhost:8081
```

O Flyway aplica automaticamente as migrations V01–V16 na primeira inicialização, incluindo dados de demonstração (2 vendedores, 3 clientes, 10 cervejas e 24 vendas).

Por hardening de segurança, não há mais credencial administrativa padrão ativa após as migrations.
Para criar um admin local manualmente, use o script `docs/sql/bootstrap-local-admin.sql`.

### Serviços Docker

| Serviço | Container | Porta host | Porta interna |
|---|---|---|---|
| Aplicação | `brewer-app` | `8081` | `8080` |
| MariaDB | `brewer-db` | `3306` | `3306` |

## Executando Localmente (sem Docker)

### Pré-requisitos
- Java 17+
- Maven 3.9+
- (opcional) MariaDB 11+ para usar perfil `prod`

### Backend

```bash
# Perfil de desenvolvimento (H2 em memória — sem banco externo)
mvn spring-boot:run

# Perfil de produção (MariaDB externo)
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

Disponível em `http://localhost:8080`.

### Frontend

```bash
cd frontend
npm install
npm start
```

Disponível em `http://localhost:4200`.

### Testes E2E

```bash
cd e2e
npm install
npx playwright install
npm test
```

### Banco de testes para profile test

Para provisionar o schema de testes e permissões em um servidor novo:

```bash
docker compose exec -T db mariadb -uroot -proot < docs/sql/bootstrap-test-db.sql
```

Depois execute os testes do backend:

```bash
mvn test
```

### Bootstrap manual de admin local

Para ambientes locais, gere um hash BCrypt, ajuste nome/e-mail e execute:

```bash
docker compose exec -T db mariadb -uroot -proot brewer < docs/sql/bootstrap-local-admin.sql
```

## Configuração

### Perfis de Aplicação

| Perfil | Banco | Descrição |
|---|---|---|
| `default` | H2 (memória) | Desenvolvimento local, sem configuração externa |
| `prod` | MariaDB | Produção via variáveis de ambiente ou `application-prod.properties` |
| `docker` | MariaDB (container) | Composição com `docker-compose.yml` |

### Variáveis de Ambiente (Docker / prod)

| Variável | Padrão | Descrição |
|---|---|---|
| `DB_HOST` | `db` | Host do MariaDB |
| `DB_PORT` | `3306` | Porta do MariaDB |
| `DB_NAME` | `brewer` | Nome do banco |
| `DB_USER` | `brewer` | Usuário do banco |
| `DB_PASSWORD` | `brewer` | Senha do banco |
| `DB_ROOT_PASSWORD` | `root` | Senha root do MariaDB |
| `MAIL_HOST` | `smtp.sendgrid.net` | Servidor SMTP |
| `MAIL_PORT` | `587` | Porta SMTP |
| `MAIL_USERNAME` | — | Usuário SMTP |
| `MAIL_PASSWORD` | — | Senha SMTP |
| `AWS_ACCESS_KEY_ID` | — | AWS Access Key (upload S3) |
| `AWS_SECRET_ACCESS_KEY` | — | AWS Secret Key |
| `AWS_S3_BUCKET` | — | Bucket S3 para fotos de cervejas |

### Controle de Acesso

O sistema usa grupos e permissões granulares:

| Grupo | Descrição |
|---|---|
| `Administrador` | Acesso total |
| `Vendedor` | Acesso a vendas e consultas |

| Role | Descrição |
|---|---|
| `ROLE_CADASTRAR_CIDADE` | Cadastrar e editar cidades |
| `ROLE_CADASTRAR_USUARIO` | Cadastrar e editar usuários |
| `ROLE_CANCELAR_VENDA` | Cancelar vendas emitidas |

## Migrações de Banco (Flyway)

| Migration | Descrição |
|---|---|
| V01 | Tabelas `estilo` e `cerveja` + estilos iniciais |
| V02–V03 | Colunas `quantidade_estoque`, `foto`, `content_type` em `cerveja` |
| V04 | Tabelas `estado` e `cidade` |
| V05–V06 | Tabela `cliente` |
| V07–V11 | Tabelas `usuario`, `grupo`, `permissao`; usuário admin; permissões |
| V12 | Tabelas `venda` e `item_venda` |
| V13 | Role `ROLE_CANCELAR_VENDA` |
| V14 | Estados e cidades do Brasil |
| V15 | Dados de demonstração: 2 vendedores, 3 clientes, 10 cervejas, 24 vendas |
| V16 | Desativa usuário admin padrão legado e remove vínculo administrativo (hardening) |

## CI/CD

- **GitHub Actions** — análise estática de segurança (OSSAR) em push/PR para `master`
- **Dependabot** — atualizações automáticas de dependências Maven e npm
- Spring MVC 5.x → Spring Boot 3.2.5
- javax.* → jakarta.* (Jakarta EE)
- Hibernate Criteria API → JPA Criteria API
- Spring Security 5 → Spring Security 6

### Plano de migração faseada do Angular

- Fase 1 (concluída em 2026-04-13): alinhar dependências do frontend em versão coordenada e manter bloqueio de upgrades major automáticos parciais no Dependabot.
- Fase 2 (baseline atual): manter upgrades coordenados por major a partir do Angular 21 (framework, CLI, builder, compiler-cli, zone.js e TypeScript compatível), sempre com validação de build/testes.
- Fase 3 (contínua): repetir o processo a cada novo major somente após CI verde e validação funcional mínima.

## Security Notes (2026-04-13)

- Estado final verificado: 0 alertas open no Dependabot e 0 alertas open no Code Scanning.
- Backlog de PRs de seguranca/dependencias: 0 PRs abertas ao final da rodada.
- Principais frentes executadas: upgrade coordenado do Angular, remocao do fluxo legado com JasperReports, hardening de DOM/XSS em JS de primeira parte e conciliacao operacional do scanner.
- Resumo tecnico completo: `docs/security-remediation-summary-2026-04-13.md`.
- PRs-chave da rodada: #54, #55, #56, #57, #58, #59, #60 e #52.

## Licença

Este projeto é baseado no curso AlgaWorks e serve apenas para fins educacionais.
