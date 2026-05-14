package com.algaworks.brewer.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class VendaModelTest {

    @Test
    void shouldAddItemsAndCalculateTotals() {
        Venda venda = new Venda();

        ItemVenda item1 = item(2, "10.00");
        ItemVenda item2 = item(1, "5.00");

        venda.adicionarItens(List.of(item1, item2));
        venda.setValorFrete(new BigDecimal("3.00"));
        venda.setValorDesconto(new BigDecimal("2.00"));

        assertThat(venda.getValorTotalItens()).isEqualByComparingTo("25.00");
        assertThat(item1.getVenda()).isSameAs(venda);
        assertThat(item2.getVenda()).isSameAs(venda);

        venda.calcularValorTotal();

        assertThat(venda.getValorTotal()).isEqualByComparingTo("26.00");
    }

    @Test
    void shouldHandleNullFreteAndDescontoWhenCalculatingTotal() {
        Venda venda = new Venda();
        venda.adicionarItens(List.of(item(1, "7.50")));
        venda.setValorFrete(null);
        venda.setValorDesconto(null);

        venda.calcularValorTotal();

        assertThat(venda.getValorTotal()).isEqualByComparingTo("7.50");
    }

    @Test
    void shouldSplitDataHoraEntregaOnPostLoadAndExposeTransientFields() {
        Venda venda = new Venda();
        LocalDateTime dataHoraEntrega = LocalDateTime.of(2025, 3, 1, 14, 45);
        venda.setDataHoraEntrega(dataHoraEntrega);
        venda.setUuid("uuid-teste");

        ReflectionTestUtils.invokeMethod(venda, "postLoad");

        assertThat(venda.getDataEntrega()).isEqualTo(LocalDate.of(2025, 3, 1));
        assertThat(venda.getHorarioEntrega()).isEqualTo(LocalTime.of(14, 45));
        assertThat(venda.getUuid()).isEqualTo("uuid-teste");
    }

    @Test
    void shouldControlSalvarByStatusAndCalculateCreationDays() {
        Venda venda = new Venda();
        venda.setDataCriacao(LocalDateTime.now().minusDays(3));

        assertThat(venda.getDiasCriacao()).isGreaterThanOrEqualTo(3);
        assertThat(venda.isSalvarPermitido()).isTrue();
        assertThat(venda.isSalvarProibido()).isFalse();

        venda.setStatus(StatusVenda.CANCELADA);

        assertThat(venda.isSalvarPermitido()).isFalse();
        assertThat(venda.isSalvarProibido()).isTrue();
    }

    @Test
    void shouldTrackIdentityByCodigoAndIsNova() {
        Venda venda = new Venda();
        assertThat(venda.isNova()).isTrue();

        venda.setCodigo(99L);
        Venda outra = new Venda();
        outra.setCodigo(99L);

        assertThat(venda.isNova()).isFalse();
        assertThat(venda).isEqualTo(outra);
        assertThat(venda.hashCode()).isEqualTo(outra.hashCode());
    }

    private static ItemVenda item(Integer quantidade, String valorUnitario) {
        ItemVenda item = new ItemVenda();
        item.setQuantidade(quantidade);
        item.setValorUnitario(new BigDecimal(valorUnitario));
        return item;
    }
}
