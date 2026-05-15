package com.algaworks.brewer.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.algaworks.brewer.model.Origem;
import com.algaworks.brewer.model.Sabor;
import com.algaworks.brewer.model.StatusVenda;
import com.algaworks.brewer.model.TipoPessoa;
import com.algaworks.brewer.model.validation.group.CnpjGroup;
import com.algaworks.brewer.model.validation.group.CpfGroup;

class DtoAndEnumTest {

    @Test
    void shouldApplyDefaultFotoInCervejaDtoWhenFotoIsNullOrEmpty() {
        CervejaDTO semFoto = new CervejaDTO(1L, "SKU-1", "IPA", Origem.NACIONAL, new BigDecimal("19.90"), null);
        CervejaDTO fotoVazia = new CervejaDTO(1L, "SKU-1", "IPA", Origem.NACIONAL, new BigDecimal("19.90"), "");
        CervejaDTO comFoto = new CervejaDTO(1L, "SKU-1", "IPA", Origem.INTERNACIONAL, new BigDecimal("19.90"), "ipa.png");

        assertThat(semFoto.getOrigem()).isEqualTo("Nacional");
        assertThat(semFoto.getFoto()).isEqualTo("cerveja-mock.png");
        assertThat(fotoVazia.getFoto()).isEqualTo("cerveja-mock.png");
        assertThat(comFoto.getFoto()).isEqualTo("ipa.png");
    }

    @Test
    void shouldExposeMutatorsForSimpleDtos() {
        FotoDTO foto = new FotoDTO("nome.png", "image/png", "/fotos/nome.png");
        foto.setNome("novo.png");
        foto.setContentType("image/jpeg");
        foto.setUrl("/fotos/novo.png");

        ValorItensEstoque valorItensEstoque = new ValorItensEstoque();
        valorItensEstoque.setValor(new BigDecimal("10.00"));
        valorItensEstoque.setTotalItens(3L);

        VendaMes vendaMes = new VendaMes("Jan", 12);
        vendaMes.setMes("Fev");
        vendaMes.setTotal(22);

        VendaOrigem vendaOrigem = new VendaOrigem("Jan", 8, 4);
        vendaOrigem.setMes("Mar");
        vendaOrigem.setTotalNacional(10);
        vendaOrigem.setTotalInternacional(5);

        assertThat(foto.getNome()).isEqualTo("novo.png");
        assertThat(foto.getContentType()).isEqualTo("image/jpeg");
        assertThat(foto.getUrl()).isEqualTo("/fotos/novo.png");
        assertThat(valorItensEstoque.getValor()).isEqualByComparingTo("10.00");
        assertThat(valorItensEstoque.getTotalItens()).isEqualTo(3L);
        assertThat(vendaMes.getMes()).isEqualTo("Fev");
        assertThat(vendaMes.getTotal()).isEqualTo(22);
        assertThat(vendaOrigem.getMes()).isEqualTo("Mar");
        assertThat(vendaOrigem.getTotalNacional()).isEqualTo(10);
        assertThat(vendaOrigem.getTotalInternacional()).isEqualTo(5);
    }

    @Test
    void shouldReturnDefaultValuesWhenValorItensEstoqueFieldsAreNull() {
        ValorItensEstoque dto = new ValorItensEstoque();

        assertThat(dto.getValor()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(dto.getTotalItens()).isEqualTo(0L);
    }

    @Test
    void shouldKeepRelatorioVendaEmitidaItemConstructorValues() {
        LocalDateTime dataCriacao = LocalDateTime.of(2024, 1, 10, 15, 30);
        RelatorioVendaEmitidaItem item = new RelatorioVendaEmitidaItem(10L, dataCriacao, "Cliente", "Usuário",
                new BigDecimal("45.90"), StatusVenda.EMITIDA);

        assertThat(item.getCodigo()).isEqualTo(10L);
        assertThat(item.getDataCriacao()).isEqualTo(dataCriacao);
        assertThat(item.getNomeCliente()).isEqualTo("Cliente");
        assertThat(item.getNomeUsuario()).isEqualTo("Usuário");
        assertThat(item.getValorTotal()).isEqualByComparingTo("45.90");
        assertThat(item.getStatus()).isEqualTo(StatusVenda.EMITIDA);
    }

    @Test
    void shouldExposeDescriptionsAndFormattingFromEnums() {
        assertThat(Origem.NACIONAL.getDescricao()).isEqualTo("Nacional");
        assertThat(Sabor.AMARGA.getDescricao()).isEqualTo("Amarga");
        assertThat(StatusVenda.CANCELADA.getDescricao()).isEqualTo("Cancelada");

        assertThat(TipoPessoa.FISICA.getDescricao()).isEqualTo("Física");
        assertThat(TipoPessoa.FISICA.getDocumento()).isEqualTo("CPF");
        assertThat(TipoPessoa.FISICA.getMascara()).isEqualTo("000.000.000-00");
        assertThat(TipoPessoa.FISICA.getGrupo()).isEqualTo(CpfGroup.class);
        assertThat(TipoPessoa.FISICA.formatar("12345678901")).isEqualTo("123.456.789-01");

        assertThat(TipoPessoa.JURIDICA.getGrupo()).isEqualTo(CnpjGroup.class);
        assertThat(TipoPessoa.JURIDICA.formatar("12345678000199")).isEqualTo("12.345.678/0001-99");
        assertThat(TipoPessoa.removerFormatacao("12.345.678/0001-99")).isEqualTo("12345678000199");
    }
}
