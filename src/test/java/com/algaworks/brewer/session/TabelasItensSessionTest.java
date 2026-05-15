package com.algaworks.brewer.session;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.algaworks.brewer.model.Cerveja;

class TabelasItensSessionTest {

    @Test
    void shouldManageItemsByUuid() {
        TabelasItensSession session = new TabelasItensSession();
        Cerveja ipa = cerveja(1L, "10.00");

        session.adicionarItem("uuid-1", ipa, 2);
        session.adicionarItem("uuid-2", cerveja(2L, "7.50"), 1);

        assertThat(session.getItens("uuid-1")).hasSize(1);
        assertThat(session.getItens("uuid-2")).hasSize(1);
        assertThat(session.getValorTotal("uuid-1")).isEqualTo(new BigDecimal("20.00"));
        assertThat(session.getValorTotal("uuid-2")).isEqualTo(new BigDecimal("7.50"));
    }

    @Test
    void shouldAlterQuantityAndExcludeItem() {
        TabelasItensSession session = new TabelasItensSession();
        Cerveja pilsen = cerveja(3L, "5.00");

        session.adicionarItem("uuid-3", pilsen, 1);
        session.alterarQuantidadeItens("uuid-3", pilsen, 4);

        assertThat(session.getValorTotal("uuid-3")).isEqualTo(new BigDecimal("20.00"));

        session.excluirItem("uuid-3", pilsen);

        assertThat(session.getItens("uuid-3")).isEmpty();
        assertThat(session.getValorTotal("uuid-3")).isEqualTo(BigDecimal.ZERO);
    }

    private static Cerveja cerveja(Long codigo, String valor) {
        Cerveja cerveja = new Cerveja();
        cerveja.setCodigo(codigo);
        cerveja.setValor(new BigDecimal(valor));
        return cerveja;
    }
}
