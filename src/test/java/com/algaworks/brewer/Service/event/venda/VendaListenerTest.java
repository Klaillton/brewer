package com.algaworks.brewer.Service.event.venda;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.algaworks.brewer.model.Cerveja;
import com.algaworks.brewer.model.ItemVenda;
import com.algaworks.brewer.model.Venda;
import com.algaworks.brewer.repository.Cervejas;

class VendaListenerTest {

    private VendaListener listener;
    private Cervejas cervejas;

    @BeforeEach
    void setup() {
        listener = new VendaListener();
        cervejas = org.mockito.Mockito.mock(Cervejas.class);
        ReflectionTestUtils.setField(listener, "cervejas", cervejas);
    }

    @Test
    void shouldDecreaseInventoryWhenSaleIsIssued() {
        Cerveja cerveja = new Cerveja();
        cerveja.setCodigo(1L);
        cerveja.setQuantidadeEstoque(10);
        when(cervejas.findById(1L)).thenReturn(Optional.of(cerveja));

        listener.vendaEmitida(new VendaEvent(novaVenda(1L, 3)));

        assertThat(cerveja.getQuantidadeEstoque()).isEqualTo(7);
        verify(cervejas).save(cerveja);
    }

    @Test
    void shouldIncreaseInventoryWhenSaleIsCancelled() {
        Cerveja cerveja = new Cerveja();
        cerveja.setCodigo(1L);
        cerveja.setQuantidadeEstoque(5);
        when(cervejas.findById(1L)).thenReturn(Optional.of(cerveja));

        listener.vendaCancelada(new CancelaVendaEvent(novaVenda(1L, 2)));

        assertThat(cerveja.getQuantidadeEstoque()).isEqualTo(7);
        verify(cervejas).save(cerveja);
    }

    private Venda novaVenda(Long codigoCerveja, int quantidade) {
        Cerveja cerveja = new Cerveja();
        cerveja.setCodigo(codigoCerveja);

        ItemVenda item = new ItemVenda();
        item.setCerveja(cerveja);
        item.setQuantidade(quantidade);
        item.setValorUnitario(BigDecimal.ONE);

        Venda venda = new Venda();
        venda.adicionarItens(java.util.List.of(item));
        return venda;
    }
}
