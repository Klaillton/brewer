package com.algaworks.brewer.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

class DomainEntitiesAndValueObjectsTest {

    @Test
    void shouldHandleCervejaPhotoHelpersAndNewFlag() {
        Cerveja cerveja = new Cerveja();

        assertThat(cerveja.getFotoOuMock()).isEqualTo("cerveja-mock.png");
        assertThat(cerveja.temFoto()).isFalse();
        assertThat(cerveja.isNova()).isTrue();

        cerveja.setFoto("ipa.png");
        cerveja.setCodigo(10L);

        assertThat(cerveja.getFotoOuMock()).isEqualTo("ipa.png");
        assertThat(cerveja.temFoto()).isTrue();
        assertThat(cerveja.isNova()).isFalse();
    }

    @Test
    void shouldComputeItemVendaTotalAndCompareByCodigo() {
        ItemVenda item = new ItemVenda();
        item.setQuantidade(3);
        item.setValorUnitario(new BigDecimal("4.50"));

        ItemVenda itemComMesmoCodigo = new ItemVenda();
        itemComMesmoCodigo.setCodigo(1L);

        ItemVenda itemComCodigoIgual = new ItemVenda();
        itemComCodigoIgual.setCodigo(1L);

        ItemVenda itemComCodigoDiferente = new ItemVenda();
        itemComCodigoDiferente.setCodigo(2L);

        assertThat(item.getValorTotal()).isEqualByComparingTo("13.50");
        assertThat(itemComMesmoCodigo).isEqualTo(itemComCodigoIgual);
        assertThat(itemComMesmoCodigo).hasSameHashCodeAs(itemComCodigoIgual);
        assertThat(itemComMesmoCodigo).isNotEqualTo(itemComCodigoDiferente);
    }

    @Test
    void shouldExposeEnderecoFromCidadeAndEstado() {
        Estado estado = new Estado();
        estado.setSigla("SP");

        Cidade cidade = new Cidade();
        cidade.setNome("Campinas");
        cidade.setEstado(estado);

        Endereco endereco = new Endereco();
        endereco.setCidade(cidade);

        Endereco semCidade = new Endereco();

        assertThat(endereco.getEstado()).isEqualTo(estado);
        assertThat(endereco.getNomeCidadeSiglaEstado()).isEqualTo("Campinas/SP");
        assertThat(semCidade.getEstado()).isNull();
        assertThat(semCidade.getNomeCidadeSiglaEstado()).isNull();
    }

    @Test
    void shouldCalculateVendaTotalsAndStatusFlags() {
        ItemVenda item = new ItemVenda();
        item.setQuantidade(2);
        item.setValorUnitario(new BigDecimal("10.00"));

        Venda venda = new Venda();
        venda.adicionarItens(List.of(item));
        venda.setValorFrete(new BigDecimal("5.00"));
        venda.setValorDesconto(new BigDecimal("3.00"));
        venda.calcularValorTotal();

        venda.setStatus(StatusVenda.CANCELADA);
        boolean salvarPermitidoCancelada = venda.isSalvarPermitido();
        boolean salvarProibidoCancelada = venda.isSalvarProibido();

        venda.setStatus(StatusVenda.EMITIDA);
        venda.setDataCriacao(LocalDate.now().minusDays(5).atStartOfDay());

        assertThat(item.getVenda()).isEqualTo(venda);
        assertThat(venda.getValorTotalItens()).isEqualByComparingTo("20.00");
        assertThat(venda.getValorTotal()).isEqualByComparingTo("22.00");
        assertThat(salvarPermitidoCancelada).isFalse();
        assertThat(salvarProibidoCancelada).isTrue();
        assertThat(venda.isSalvarPermitido()).isTrue();
        assertThat(venda.isSalvarProibido()).isFalse();
        assertThat(venda.getDiasCriacao()).isEqualTo(5L);

        venda.setCodigo(99L);
        assertThat(venda.isNova()).isFalse();

        Venda vendaSemDataCriacao = new Venda();
        assertThat(vendaSemDataCriacao.getDiasCriacao()).isEqualTo(0L);
        assertThat(vendaSemDataCriacao.isNova()).isTrue();
    }

    @Test
    void shouldCompareUsuarioGrupoAndIdByTheirIdentifiers() {
        Usuario usuarioA = new Usuario();
        usuarioA.setCodigo(1L);

        Usuario usuarioB = new Usuario();
        usuarioB.setCodigo(1L);

        Grupo grupoA = new Grupo();
        grupoA.setCodigo(2L);

        Grupo grupoB = new Grupo();
        grupoB.setCodigo(2L);

        UsuarioGrupoId id1 = new UsuarioGrupoId();
        id1.setUsuario(usuarioA);
        id1.setGrupo(grupoA);

        UsuarioGrupoId id2 = new UsuarioGrupoId();
        id2.setUsuario(usuarioB);
        id2.setGrupo(grupoB);

        UsuarioGrupo usuarioGrupo1 = new UsuarioGrupo();
        usuarioGrupo1.setId(id1);

        UsuarioGrupo usuarioGrupo2 = new UsuarioGrupo();
        usuarioGrupo2.setId(id2);

        assertThat(id1).isEqualTo(id2);
        assertThat(id1).hasSameHashCodeAs(id2);
        assertThat(usuarioGrupo1).isEqualTo(usuarioGrupo2);
        assertThat(usuarioGrupo1).hasSameHashCodeAs(usuarioGrupo2);
    }

    @Test
    void shouldApplySimpleFlagsInCidadeAndUsuario() {
        Cidade cidade = new Cidade();
        Usuario usuario = new Usuario();

        assertThat(cidade.temEstado()).isFalse();
        assertThat(cidade.isNova()).isTrue();
        assertThat(usuario.isNovo()).isTrue();

        cidade.setEstado(new Estado());
        cidade.setCodigo(7L);
        usuario.setCodigo(8L);

        assertThat(cidade.temEstado()).isTrue();
        assertThat(cidade.isNova()).isFalse();
        assertThat(usuario.isNovo()).isFalse();
    }

    @Test
    void shouldComparePermissaoByCodigo() {
        Permissao permissaoA = new Permissao();
        permissaoA.setCodigo(5L);

        Permissao permissaoB = new Permissao();
        permissaoB.setCodigo(5L);

        Permissao permissaoC = new Permissao();
        permissaoC.setCodigo(6L);

        assertThat(permissaoA).isEqualTo(permissaoB);
        assertThat(permissaoA).hasSameHashCodeAs(permissaoB);
        assertThat(permissaoA).isNotEqualTo(permissaoC);
    }

    @Test
    void shouldSplitDataHoraEntregaIntoDataAndHorarioAfterPostLoad() throws Exception {
        Venda venda = new Venda();
        LocalDateTime dataHoraEntrega = LocalDateTime.of(2025, 6, 20, 18, 45);
        venda.setDataHoraEntrega(dataHoraEntrega);

        var postLoad = Venda.class.getDeclaredMethod("postLoad");
        postLoad.setAccessible(true);
        postLoad.invoke(venda);

        assertThat(venda.getDataEntrega()).isEqualTo(dataHoraEntrega.toLocalDate());
        assertThat(venda.getHorarioEntrega()).isEqualTo(dataHoraEntrega.toLocalTime());
    }
}
