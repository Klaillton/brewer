# Brewer - Sistema de Gerenciamento de Cervejas

Sistema de gerenciamento de cervejaria desenvolvido com Spring Boot 3.x e Angular.

## Tecnologias

### Backend
- **Java 17** - Linguagem de programação
- **Spring Boot 3.2.5** - Framework principal
- **Spring Security 6** - Autenticação e autorização
- **Spring Data JPA** - Persistência de dados
- **Hibernate 6** - ORM
- **Thymeleaf** - Template engine para views tradicionais
- **H2 Database** - Banco de dados em memória para desenvolvimento

### Frontend
- **Angular 17** - Framework SPA
- **TypeScript 5.3** - Linguagem de programação tipada
- **RxJS** - Programação reativa

### Testes
- **JUnit 5** - Testes unitários
- **Playwright** - Testes E2E

## Estrutura do Projeto

```
brewer/
├── src/main/java/           # Código-fonte Java
│   └── com/algaworks/brewer/
│       ├── api/             # Controladores REST API
│       ├── config/          # Configurações Spring
│       ├── controller/      # Controladores MVC
│       ├── model/           # Entidades JPA
│       ├── repository/      # Repositórios Spring Data
│       └── Service/         # Camada de serviços
├── src/main/resources/      # Recursos (templates, configs)
├── frontend/                # Aplicação Angular
│   └── src/
│       └── app/
│           ├── components/  # Componentes Angular
│           ├── models/      # Modelos TypeScript
│           └── services/    # Serviços HTTP
└── e2e/                     # Testes E2E Playwright
    └── tests/
```

## API REST

O sistema expõe as seguintes APIs RESTful:

| Recurso | Endpoint | Métodos |
|---------|----------|---------|
| Cervejas | `/api/cervejas` | GET, POST, PUT, DELETE |
| Estilos | `/api/estilos` | GET, POST, PUT, DELETE |
| Clientes | `/api/clientes` | GET, POST, PUT, DELETE |
| Cidades | `/api/cidades` | GET, POST, PUT |
| Estados | `/api/estados` | GET |
| Vendas | `/api/vendas` | GET |
| Usuários | `/api/usuarios` | GET, POST, PUT |

## Executando o Projeto

### Backend

```bash
# Compilar
mvn clean compile

# Executar testes
mvn test

# Iniciar aplicação
mvn spring-boot:run
```

A aplicação estará disponível em `http://localhost:8080`

### Frontend

```bash
cd frontend
npm install
npm start
```

A aplicação Angular estará disponível em `http://localhost:4200`

### Testes E2E

```bash
cd e2e
npm install
npx playwright install
npm test
```

## Configuração

### application.properties

O arquivo `src/main/resources/application.properties` contém as configurações da aplicação:

- **Banco de dados**: H2 em memória (desenvolvimento)
- **JPA**: Auto-criação de schema
- **Thymeleaf**: Templates em `classpath:/templates/`

### Variáveis de Ambiente

| Variável | Descrição |
|----------|-----------|
| `aws.access.key.id` | AWS Access Key para S3 |
| `aws.secret.access.key` | AWS Secret Key para S3 |
| `mail.username` | Usuário do servidor de email |
| `mail.password` | Senha do servidor de email |

## Migração de Versões

Este projeto foi migrado de:
- Java 8 → Java 17
- Spring MVC 5.x → Spring Boot 3.2.5
- javax.* → jakarta.* (Jakarta EE)
- Hibernate Criteria API → JPA Criteria API
- Spring Security 5 → Spring Security 6

## Licença

Este projeto é baseado no curso AlgaWorks e serve apenas para fins educacionais.
