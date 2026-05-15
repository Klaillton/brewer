package com.algaworks.brewer.repository.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.algaworks.brewer.model.Estado;
import com.algaworks.brewer.model.Estilo;
import com.algaworks.brewer.model.Grupo;
import com.algaworks.brewer.model.Origem;
import com.algaworks.brewer.model.Sabor;
import com.algaworks.brewer.model.StatusVenda;

class RepositoryFiltersTest {

    @Test
    void shouldMapCervejaFilterFields() {
        Estilo estilo = new Estilo();
        estilo.setCodigo(1L);
        CervejaFilter filtro = new CervejaFilter();

        filtro.setSku("SKU-123");
        filtro.setNome("IPA");
        filtro.setEstilo(estilo);
        filtro.setSabor(Sabor.FORTE);
        filtro.setOrigem(Origem.NACIONAL);
        filtro.setValorDe(new BigDecimal("10.00"));
        filtro.setValorAte(new BigDecimal("50.00"));

        assertThat(filtro.getSku()).isEqualTo("SKU-123");
        assertThat(filtro.getNome()).isEqualTo("IPA");
        assertThat(filtro.getEstilo()).isEqualTo(estilo);
        assertThat(filtro.getSabor()).isEqualTo(Sabor.FORTE);
        assertThat(filtro.getOrigem()).isEqualTo(Origem.NACIONAL);
        assertThat(filtro.getValorDe()).isEqualByComparingTo("10.00");
        assertThat(filtro.getValorAte()).isEqualByComparingTo("50.00");
    }

    @Test
    void shouldMapCidadeAndEstiloFilters() {
        Estado estado = new Estado();
        estado.setCodigo(35L);

        CidadeFilter cidadeFilter = new CidadeFilter();
        cidadeFilter.setNome("Campinas");
        cidadeFilter.setEstado(estado);

        EstiloFilter estiloFilter = new EstiloFilter();
        estiloFilter.setCodigo(2L);
        estiloFilter.setNome("Pilsen");

        assertThat(cidadeFilter.getNome()).isEqualTo("Campinas");
        assertThat(cidadeFilter.getEstado()).isEqualTo(estado);
        assertThat(estiloFilter.getCodigo()).isEqualTo(2L);
        assertThat(estiloFilter.getNome()).isEqualTo("Pilsen");
    }

    @Test
    void shouldMapClienteAndUsuarioFilters() {
        ClienteFilter clienteFilter = new ClienteFilter();
        clienteFilter.setNome("Maria");
        clienteFilter.setCpfOuCnpj("123.456.789-01");

        Grupo grupo = new Grupo();
        grupo.setCodigo(10L);
        UsuarioFilter usuarioFilter = new UsuarioFilter();
        usuarioFilter.setNome("Admin");
        usuarioFilter.setEmail("admin@brewer.com");
        usuarioFilter.setGrupos(List.of(grupo));

        assertThat(clienteFilter.getNome()).isEqualTo("Maria");
        assertThat(clienteFilter.getCpfOuCnpj()).isEqualTo("123.456.789-01");
        assertThat(clienteFilter.getCpfOuCnpjSemFormatacao()).isEqualTo("12345678901");
        assertThat(usuarioFilter.getNome()).isEqualTo("Admin");
        assertThat(usuarioFilter.getEmail()).isEqualTo("admin@brewer.com");
        assertThat(usuarioFilter.getGrupos()).containsExactly(grupo);
    }

    @Test
    void shouldMapVendaFilterFields() {
        VendaFilter filtro = new VendaFilter();

        filtro.setCodigo(99L);
        filtro.setStatus(StatusVenda.ORCAMENTO);
        filtro.setDesde(LocalDate.of(2024, 1, 1));
        filtro.setAte(LocalDate.of(2024, 1, 31));
        filtro.setValorMinimo(new BigDecimal("30.00"));
        filtro.setValorMaximo(new BigDecimal("200.00"));
        filtro.setNomeCliente("Cliente X");
        filtro.setCpfOuCnpjCliente("12.345.678/0001-99");

        assertThat(filtro.getCodigo()).isEqualTo(99L);
        assertThat(filtro.getStatus()).isEqualTo(StatusVenda.ORCAMENTO);
        assertThat(filtro.getDesde()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(filtro.getAte()).isEqualTo(LocalDate.of(2024, 1, 31));
        assertThat(filtro.getValorMinimo()).isEqualByComparingTo("30.00");
        assertThat(filtro.getValorMaximo()).isEqualByComparingTo("200.00");
        assertThat(filtro.getNomeCliente()).isEqualTo("Cliente X");
        assertThat(filtro.getCpfOuCnpjCliente()).isEqualTo("12.345.678/0001-99");
    }
}
