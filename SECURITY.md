# Security Policy

## Versões Suportadas

Este é um projeto de estudo/portfólio. Apenas o branch `master` recebe correções de segurança.

| Versão / Branch | Suportada |
|---|---|
| `master` | ✅ Sim |
| Branches antigas ou forks | ❌ Não |

## Reportando uma Vulnerabilidade

**Não abra uma issue pública** para relatar vulnerabilidades de segurança.

Use o fluxo de **reporte privado** do GitHub:

1. Acesse a aba **Security** do repositório
2. Clique em **Report a vulnerability**
3. Descreva o problema com o máximo de detalhes

Ao reportar, inclua:
- Descrição clara da vulnerabilidade e área afetada
- Passos para reprodução ou prova de conceito
- Avaliação de impacto (se conhecida)
- Sugestão de correção (se disponível)

**Prazo de resposta esperado:** confirmação inicial em até 5 dias úteis.

Por favor, evite divulgação pública até que o problema tenha sido revisado e uma correção ou mitigação esteja disponível.

---

## Práticas de Segurança do Projeto

### Autenticação e Autorização
- Autenticação via **form login** com Spring Security 6
- Senhas armazenadas com **BCrypt** (fator de custo padrão do Spring Security)
- Controle de acesso baseado em **roles** (`ROLE_CADASTRAR_CIDADE`, `ROLE_CADASTRAR_USUARIO`, `ROLE_CANCELAR_VENDA`) e **grupos** (Administrador, Vendedor)
- Proteção contra **CSRF** habilitada por padrão no Spring Security
- Cabeçalhos de segurança HTTP configurados via Spring Security (X-Content-Type-Options, X-Frame-Options, etc.)

### Banco de Dados
- Migrations versionadas com **Flyway** — sem DDL automático em produção
- Queries parametrizadas via **Spring Data JPA / JPQL** — sem SQL concatenado (proteção contra SQL Injection)
- Credenciais do banco injetadas por **variáveis de ambiente** (nunca em código ou no repositório)
- Arquivo `.env` listado no `.gitignore`

### Upload de Arquivos
- Validação de tipo MIME em uploads de fotos de cervejas
- Tamanho máximo configurado (10 MB)
- Armazenamento em **AWS S3** (produção) ou diretório local — sem execução de arquivos enviados

### Docker / Infraestrutura
- Container da aplicação executa com **usuário não-root** (`brewer`)
- Jar com permissões `644` (não executável diretamente pelo sistema)
- Imagem de runtime baseada em `eclipse-temurin:17-jre` (Debian) — sem ferramentas de build na imagem final
- Secrets via variáveis de ambiente no `docker-compose.yml` com suporte a arquivo `.env`

### Dependências
- **Dependabot** configurado para atualização automática de dependências Maven e npm
- **GitHub Actions OSSAR** (análise estática de segurança) executa em cada push/PR para `master`

### O que NÃO está implementado (escopo de estudo)
- HTTPS / TLS (esperado ser terminado no load balancer/proxy reverso)
- Rate limiting / proteção contra brute force no login
- Auditoria de ações (log de quem fez o quê)
