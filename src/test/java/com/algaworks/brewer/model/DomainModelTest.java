package com.algaworks.brewer.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class DomainModelTest {

    @Test
    void shouldApplyBusinessHelpersOnCerveja() {
        Cerveja cerveja = new Cerveja();
        cerveja.setSku("abc-123");

        ReflectionTestUtils.invokeMethod(cerveja, "prePersistUpdate");

        assertThat(cerveja.getSku()).isEqualTo("ABC-123");
        assertThat(cerveja.isNova()).isTrue();
        assertThat(cerveja.getFotoOuMock()).isEqualTo("cerveja-mock.png");
        assertThat(cerveja.temFoto()).isFalse();

        cerveja.setCodigo(1L);
        cerveja.setFoto("ipa.png");

        assertThat(cerveja.isNova()).isFalse();
        assertThat(cerveja.getFotoOuMock()).isEqualTo("ipa.png");
        assertThat(cerveja.temFoto()).isTrue();

        Cerveja cervejaComMesmoCodigo = new Cerveja();
        cervejaComMesmoCodigo.setCodigo(1L);

        assertThat(cerveja).isEqualTo(cervejaComMesmoCodigo);
        assertThat(cerveja.hashCode()).isEqualTo(cervejaComMesmoCodigo.hashCode());
    }

    @Test
    void shouldApplyFormattingAndLifecycleHelpersOnCliente() {
        Cliente cliente = new Cliente();
        cliente.setTipoPessoa(TipoPessoa.FISICA);
        cliente.setCpfOuCnpj("123.456.789-01");

        assertThat(cliente.getCpfOuCnpjSemFormatacao()).isEqualTo("12345678901");
        assertThat(cliente.isNova()).isTrue();

        ReflectionTestUtils.invokeMethod(cliente, "prePersistPreUpdate");
        assertThat(cliente.getCpfOuCnpj()).isEqualTo("12345678901");

        ReflectionTestUtils.invokeMethod(cliente, "postLoad");
        assertThat(cliente.getCpfOuCnpj()).isEqualTo("123.456.789-01");

        cliente.setCodigo(10L);
        Cliente outro = new Cliente();
        outro.setCodigo(10L);

        assertThat(cliente.isNova()).isFalse();
        assertThat(cliente).isEqualTo(outro);
    }

    @Test
    void shouldExposeEnderecoCidadeAndEstadoData() {
        Estado estado = new Estado();
        estado.setSigla("SP");

        Cidade cidade = new Cidade();
        cidade.setNome("Campinas");
        cidade.setEstado(estado);

        Endereco endereco = new Endereco();
        endereco.setLogradouro("Rua A");
        endereco.setNumero("123");
        endereco.setComplemento("Sala 1");
        endereco.setCep("13000-000");
        endereco.setCidade(cidade);

        assertThat(endereco.getLogradouro()).isEqualTo("Rua A");
        assertThat(endereco.getNumero()).isEqualTo("123");
        assertThat(endereco.getComplemento()).isEqualTo("Sala 1");
        assertThat(endereco.getCep()).isEqualTo("13000-000");
        assertThat(endereco.getEstado()).isEqualTo(estado);
        assertThat(endereco.getNomeCidadeSiglaEstado()).isEqualTo("Campinas/SP");
    }

    @Test
    void shouldEvaluateCidadeEstadoEstiloGrupoPermissaoHelpers() {
        Estado estado = new Estado();
        estado.setCodigo(35L);
        estado.setNome("São Paulo");
        estado.setSigla("SP");

        Cidade cidade = new Cidade();
        cidade.setCodigo(1L);
        cidade.setNome("Campinas");
        cidade.setEstado(estado);

        Cidade cidadeMesmoCodigo = new Cidade();
        cidadeMesmoCodigo.setCodigo(1L);

        assertThat(cidade.temEstado()).isTrue();
        assertThat(cidade.isNova()).isFalse();
        assertThat(cidade).isEqualTo(cidadeMesmoCodigo);

        Estilo estilo = new Estilo();
        estilo.setCodigo(2L);
        estilo.setNome("IPA");
        assertThat(estilo.isNova()).isFalse();
        assertThat(estilo.getNome()).isEqualTo("IPA");

        Grupo grupo = new Grupo();
        grupo.setCodigo(3L);
        grupo.setNome("Administradores");
        grupo.setPermissoes(List.of());
        assertThat(grupo.getNome()).isEqualTo("Administradores");

        Permissao permissao = new Permissao();
        permissao.setCodigo(5L);
        permissao.setNome("ROLE_ADMIN");
        Permissao permissaoMesmoCodigo = new Permissao();
        permissaoMesmoCodigo.setCodigo(5L);

        assertThat(permissao.getNome()).isEqualTo("ROLE_ADMIN");
        assertThat(permissao).isEqualTo(permissaoMesmoCodigo);

        Estado estadoMesmoCodigo = new Estado();
        estadoMesmoCodigo.setCodigo(35L);
        assertThat(estado).isEqualTo(estadoMesmoCodigo);
    }

    @Test
    void shouldCalculateItemVendaTotalAndTrackEqualityByCodigo() {
        ItemVenda item = new ItemVenda();
        item.setCodigo(8L);
        item.setQuantidade(3);
        item.setValorUnitario(new BigDecimal("12.50"));

        ItemVenda outro = new ItemVenda();
        outro.setCodigo(8L);

        assertThat(item.getValorTotal()).isEqualByComparingTo("37.50");
        assertThat(item).isEqualTo(outro);
        assertThat(item.hashCode()).isEqualTo(outro.hashCode());
    }

    @Test
    void shouldExposeUsuarioAndUsuarioGrupoIdentityBehavior() {
        Usuario usuario = new Usuario();
        usuario.setCodigo(11L);
        usuario.setNome("Admin");
        usuario.setEmail("admin@brewer.com");
        usuario.setSenha("123");
        usuario.setDataNascimento(LocalDate.of(1990, 1, 1));

        assertThat(usuario.isNovo()).isFalse();

        ReflectionTestUtils.invokeMethod(usuario, "preUpdate");
        assertThat(usuario.getConfirmacaoSenha()).isEqualTo("123");

        Grupo grupo = new Grupo();
        grupo.setCodigo(22L);

        UsuarioGrupoId id = new UsuarioGrupoId();
        id.setUsuario(usuario);
        id.setGrupo(grupo);

        UsuarioGrupoId outroId = new UsuarioGrupoId();
        outroId.setUsuario(usuario);
        outroId.setGrupo(grupo);

        assertThat(id).isEqualTo(outroId);

        UsuarioGrupo usuarioGrupo = new UsuarioGrupo();
        usuarioGrupo.setId(id);

        UsuarioGrupo outroUsuarioGrupo = new UsuarioGrupo();
        outroUsuarioGrupo.setId(outroId);

        assertThat(usuarioGrupo).isEqualTo(outroUsuarioGrupo);
    }
}
