# Plano de Cobertura de Testes — Brewer

> Documento gerado em 2026-05-15 com base na análise de cobertura JaCoCo (Java 25 + agente 0.8.13).

---

## Situação antes da Fase 1

| Pacote | Cobertura | Observação |
|--------|-----------|------------|
| `api` (REST Controllers) | ~9% | Quase sem cobertura |
| `Service` (lógica de negócio) | ~5% | Crítico |
| `repository.helper.*` | ~1% | Crítico |
| `validation.validator` | 0% | Sem cobertura |
| `storage.s3` | 0% | Sem cobertura |
| `model.validation` | 0% | Sem cobertura |
| `Service/Exception` | 0% | Sem cobertura |
| `session` | parcial | TabelaItensVendaTest existia |

**Total de métodos de teste pré-existentes**: 14  
**Total de classes de produção**: 123  
**Classes sem cobertura estimadas**: ~43

---

## Fase 1 — Domínio, helpers e utilitários puros ✅ CONCLUÍDA

**Branch**: `copilot/explore-test-structure`  
**PR**: [#103 — Expand Phase 1 unit coverage](https://github.com/Klaillton/brewer/pull/103)  
**Estratégia**: testes unitários puros (sem Spring context, sem banco de dados), usando JUnit 5 + Mockito.

### Arquivos de teste adicionados

| Arquivo de teste | Pacote | Métodos | Classes cobertas |
|-----------------|--------|---------|-----------------|
| `ServiceExceptionsTest.java` | `Service.Exception` | 1 | 6 exceções customizadas |
| `BigDecimalFormatterTest.java` | `config.format` | 1 | `BigDecimalFormatter` |
| `DtoAndEnumTest.java` | `dto` | 5 | `CervejaDTO`, `FotoDTO`, `VendaMes`, `VendaOrigem`, `ValorItensEstoque` |
| `DomainEntitiesAndValueObjectsTest.java` | `model` | 8 | `Cidade`, `Estado`, `Estilo`, `Grupo`, `Permissao`, `Endereco`, `ItemVenda`, `UsuarioGrupo` |
| `DomainModelTest.java` | `model` | 6 | `Cliente`, `Usuario`, `Cerveja`, `Origem`, `Sabor`, `TipoPessoa` |
| `VendaModelTest.java` | `model` | 5 | `Venda`, `StatusVenda`, `ItemVenda` (cálculos) |
| `ClienteGroupSequenceProviderTest.java` | `model.validation` | 2 | `ClienteGroupSequenceProvider` |
| `RepositoryFiltersTest.java` | `repository.filter` | 4 | `CervejaFilter`, `VendaFilter`, `ClienteFilter`, `UsuarioFilter` |
| `GeracaoDeSenhaTest.java` | `security` | 1 | `GeracaoDeSenha` |
| `UsuarioSistemaTest.java` | `security` | 1 | `UsuarioSistema` |
| `StatusUsuarioTest.java` | `service` | 2 | `StatusUsuario` |
| `TabelasItensSessionTest.java` | `session` | 2 | `TabelasItensSession` |
| `FotoStorageDefaultMethodTest.java` | `storage` | 1 | `FotoStorage` (default methods) |
| `AtributoConfirmacaoValidatorTest.java` | `validation.validator` | 3 | `AtributoConfirmacaoValidator`, `AtributosConfirmacao` |

**Novos métodos de teste**: 42  
**Total acumulado de métodos**: 56 (14 pré-existentes + 42 novos)

---

## Fase 2 — Service layer (lógica de negócio) ✅ CONCLUÍDA

**Estratégia**: testes unitários com Mockito — sem Spring context, sem banco.  
**Branch**: `copilot/prepare-execute-phase-2`  
**PR**: Em andamento

| Classe alvo | Dependências a mockar | Cenários principais |
|-------------|----------------------|---------------------|
| `CadastroEstiloService` | `Estilos` | salvar duplicado → `NomeEstiloJaCadastradoException`; salvar novo; excluir referenciado → `ImpossivelExcluirEntidadeException` |
| `CadastroCidadeService` | `Cidades`, `Estados` | nome duplicado → `NomeCidadeJaCadastradaException`; cidade com estado válido |
| `CadastroClienteService` | `Clientes` | CPF/CNPJ duplicado → `CpfCnpjClienteJaCadastradoException` |
| `CadastroUsuarioService` | `Usuarios` | usuário duplicado → `UsuarioJaCadastradoException`; senha obrigatória → `SenhaObrigatoriaUsuarioException` |
| `CadastroCervejaService` | `Cervejas`, `FotoStorage` | salvar com foto nova; substituir foto existente; excluir foto antiga |
| `CadastroVendaService` | `Vendas`, `ApplicationEventPublisher` | emitir venda; cancelar venda; publicar eventos `VendaEvent`/`CancelaVendaEvent` |
| `VendaListener` | `Cervejas` | processar `VendaEvent`; processar `CancelaVendaEvent` |
| `AppUserDetailsService` | `Usuarios` | usuário encontrado → `UsuarioSistema`; não encontrado → `UsernameNotFoundException` |

### Arquivos de teste adicionados

| Arquivo de teste | Pacote | Métodos | Classes cobertas |
|-----------------|--------|---------|-----------------|
| `CadastroEstiloServiceTest.java` | `Service` | 3 | `CadastroEstiloService` |
| `CadastroCidadeServiceTest.java` | `Service` | 3 | `CadastroCidadeService` |
| `CadastroClienteServiceTest.java` | `Service` | 3 | `CadastroClienteService` |
| `CadastroUsuarioServiceTest.java` | `Service` | 6 | `CadastroUsuarioService` |
| `CadastroCervejaServiceTest.java` | `Service` | 3 | `CadastroCervejaService` |
| `CadastroVendaServiceTest.java` | `Service` | 7 | `CadastroVendaService` |
| `VendaListenerTest.java` | `Service.event.venda` | 2 | `VendaListener` |
| `AppUserDetailsServiceTest.java` | `security` | 2 | `AppUserDetailsService` |

**Novos métodos de teste na fase 2**: 29

---

## Fase 3 — REST Controllers (api/) ✅ CONCLUÍDA

**Estratégia**: testes de controller com `MockMvc` (standalone) + Mockito — sem Spring context, sem banco.  
**Branch**: `copilot/prepare-execute-phase-3`  
**PR**: Em andamento

| Controller | Endpoints | Cenários |
|------------|-----------|---------|
| `EstadoRestController` | `GET /api/estados` | lista retornada; lista vazia |
| `EstiloRestController` | `GET /api/estilos` | lista retornada; filtro por nome |
| `CidadeRestController` | `GET /api/cidades`, `GET /api/cidades/estado/{codigo}` | filtro por estado; sem filtro |
| `CervejaRestController` | `GET /api/cervejas/search` | busca com nome/SKU; busca vazia |
| `ClienteRestController` | `GET /api/clientes` | busca por nome/CPF/CNPJ |
| `UsuarioRestController` | `GET /api/usuarios` | lista; acesso sem autenticação |
| `VendaRestController` | `GET /api/vendas/{codigo}` | venda existente; não encontrada → 404 |

### Arquivos de teste adicionados

| Arquivo de teste | Pacote | Métodos | Classes cobertas |
|-----------------|--------|---------|-----------------|
| `EstadoRestControllerTest.java` | `api` | 2 | `EstadoRestController` |
| `EstiloRestControllerTest.java` | `api` | 2 | `EstiloRestController` |
| `CidadeRestControllerTest.java` | `api` | 2 | `CidadeRestController` |
| `CervejaRestControllerTest.java` | `api` | 2 | `CervejaRestController` |
| `ClienteRestControllerTest.java` | `api` | 2 | `ClienteRestController` |
| `UsuarioRestControllerTest.java` | `api` | 2 | `UsuarioRestController` |
| `VendaRestControllerTest.java` | `api` | 2 | `VendaRestController` |

**Novos métodos de teste na fase 3**: 14

---

## Fase 4 — repository.helper.* (queries customizadas) ⏳ PLANEJADA

**Estratégia**: `@DataJpaTest` com banco MariaDB via perfil `test` ou Testcontainers.  
**Dependência**: infraestrutura de banco de testes (`brewer_test`).  
**Branch sugerida**: `test/phase-4-repository-integration`

| Implementação | Queries relevantes |
|--------------|-------------------|
| `CervejasImpl` | busca paginada com filtros (nome, estilo, sabor, valor) |
| `VendasImpl` | busca com período, status, cliente; dados para dashboard |
| `ClientesImpl` | busca por CPF/CNPJ, nome, tipo de pessoa |
| `UsuariosImpl` | busca por email, grupo |
| `CidadesImpl` | busca filtrada por estado |
| `EstilosImpl` | busca paginada por nome |

**Estimativa de novos métodos**: ~30–40  
**Observação**: requer `GRANT ALL ON brewer_test.* TO 'brewer'@'%'` no MariaDB local.

---

## Resumo e roadmap

| Fase | Escopo | Status | Métodos de teste | Branch/PR |
|------|--------|--------|-----------------|-----------|
| 0 (baseline) | Testes pré-existentes | ✅ | 14 | — |
| 1 | Domínio, helpers, utilitários | ✅ | +42 | PR #103 |
| 2 | Service layer | ✅ | +29 | PR atual |
| 3 | REST Controllers | ✅ | +14 | PR atual |
| 4 | repository.helper.* | ⏳ | +~35 | A criar |
| **Total estimado** | | | **~146** | |

---

## Cobertura esperada ao final das 4 fases

| Pacote | Antes | Após Fase 1 | Após Fase 2 | Após Fase 4 |
|--------|-------|-------------|-------------|-------------|
| `model.*` | ~5% | ~80% | ~80% | ~80% |
| `Service.*` | ~5% | ~10% | ~75% | ~75% |
| `api.*` | ~9% | ~9% | ~9% | ~9% |
| `repository.helper.*` | ~1% | ~1% | ~1% | ~70% |
| `validation.*` | 0% | ~90% | ~90% | ~90% |
| `storage.*` | 0% | ~60% | ~60% | ~60% |
| `security.*` | 0% | ~70% | ~85% | ~85% |
| `session.*` | parcial | ~85% | ~85% | ~85% |
| **Global estimado** | <10% | ~35% | ~55% | ~70% |
