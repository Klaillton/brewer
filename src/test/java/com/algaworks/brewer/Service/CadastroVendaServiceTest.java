package com.algaworks.brewer.Service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import com.algaworks.brewer.Service.event.venda.CancelaVendaEvent;
import com.algaworks.brewer.Service.event.venda.VendaEvent;
import com.algaworks.brewer.model.Cerveja;
import com.algaworks.brewer.model.ItemVenda;
import com.algaworks.brewer.model.StatusVenda;
import com.algaworks.brewer.model.Venda;
import com.algaworks.brewer.repository.Cervejas;
import com.algaworks.brewer.repository.Vendas;

class CadastroVendaServiceTest {

    private CadastroVendaService service;
    private Vendas vendas;
    private Cervejas cervejas;
    private ApplicationEventPublisher publisher;

    @BeforeEach
    void setup() {
        service = new CadastroVendaService();
        vendas = org.mockito.Mockito.mock(Vendas.class);
        cervejas = org.mockito.Mockito.mock(Cervejas.class);
        publisher = org.mockito.Mockito.mock(ApplicationEventPublisher.class);
        ReflectionTestUtils.setField(service, "vendas", vendas);
        ReflectionTestUtils.setField(service, "cervejas", cervejas);
        ReflectionTestUtils.setField(service, "publisher", publisher);
    }

    @Test
    void shouldSaveNewSaleSettingCreationDateAndBeerReference() {
        Venda venda = novaVenda(1L, true);
        Cerveja cervejaRef = new Cerveja();
        when(cervejas.getReferenceById(1L)).thenReturn(cervejaRef);
        when(vendas.saveAndFlush(venda)).thenReturn(venda);

        Venda salva = service.salvar(venda);

        assertThat(salva).isSameAs(venda);
        assertThat(venda.getDataCriacao()).isNotNull();
        assertThat(venda.getItens().get(0).getCerveja()).isSameAs(cervejaRef);
    }

    @Test
    void shouldKeepCreationDateWhenUpdatingSale() {
        Venda venda = novaVenda(1L, false);
        LocalDateTime dataCriacaoExistente = LocalDateTime.of(2025, 1, 10, 8, 0);
        Venda vendaExistente = new Venda();
        vendaExistente.setDataCriacao(dataCriacaoExistente);

        when(vendas.findById(1L)).thenReturn(Optional.of(vendaExistente));
        when(cervejas.getReferenceById(1L)).thenReturn(new Cerveja());
        when(vendas.saveAndFlush(venda)).thenReturn(venda);

        service.salvar(venda);

        assertThat(venda.getDataCriacao()).isEqualTo(dataCriacaoExistente);
    }

    @Test
    void shouldDefaultDeliveryTimeToNoonWhenOnlyDateIsProvided() {
        Venda venda = novaVenda(1L, true);
        venda.setDataEntrega(LocalDate.of(2025, 5, 2));
        venda.setHorarioEntrega(null);
        when(cervejas.getReferenceById(1L)).thenReturn(new Cerveja());
        when(vendas.saveAndFlush(venda)).thenReturn(venda);

        service.salvar(venda);

        assertThat(venda.getDataHoraEntrega()).isEqualTo(LocalDateTime.of(2025, 5, 2, 12, 0));
    }

    @Test
    void shouldBlockSaveWhenSaleIsForbidden() {
        Venda venda = novaVenda(1L, true);
        venda.setStatus(StatusVenda.CANCELADA);

        assertThatThrownBy(() -> service.salvar(venda)).isInstanceOf(RuntimeException.class);
        verify(vendas, never()).saveAndFlush(any(Venda.class));
    }

    @Test
    void shouldEmitSaleAndPublishEvent() {
        Venda venda = novaVenda(1L, true);
        when(cervejas.getReferenceById(1L)).thenReturn(new Cerveja());
        when(vendas.saveAndFlush(venda)).thenReturn(venda);

        service.emitir(venda);

        assertThat(venda.getStatus()).isEqualTo(StatusVenda.EMITIDA);
        verify(publisher).publishEvent(any(VendaEvent.class));
    }

    @Test
    void shouldCancelSaleAndPublishCancellationEventWhenPreviouslyIssued() {
        Venda vendaEntrada = new Venda();
        vendaEntrada.setCodigo(9L);
        vendaEntrada.setStatus(StatusVenda.EMITIDA);
        Venda vendaExistente = new Venda();
        vendaExistente.setCodigo(9L);
        vendaExistente.setStatus(StatusVenda.ORCAMENTO);

        when(vendas.findById(9L)).thenReturn(Optional.of(vendaExistente));

        service.cancelar(vendaEntrada);

        assertThat(vendaExistente.getStatus()).isEqualTo(StatusVenda.CANCELADA);
        verify(vendas).save(vendaExistente);
        verify(publisher).publishEvent(any(CancelaVendaEvent.class));
    }

    @Test
    void shouldCancelSaleWithoutPublishingEventWhenInputIsNotIssued() {
        Venda vendaEntrada = new Venda();
        vendaEntrada.setCodigo(9L);
        vendaEntrada.setStatus(StatusVenda.ORCAMENTO);
        Venda vendaExistente = new Venda();
        vendaExistente.setCodigo(9L);

        when(vendas.findById(9L)).thenReturn(Optional.of(vendaExistente));

        service.cancelar(vendaEntrada);

        verify(vendas).save(vendaExistente);
        verify(publisher, never()).publishEvent(any(CancelaVendaEvent.class));
    }

    private Venda novaVenda(Long codigoCerveja, boolean novaVenda) {
        Venda venda = new Venda();
        if (!novaVenda) {
            venda.setCodigo(1L);
        }

        Cerveja cervejaItem = new Cerveja();
        cervejaItem.setCodigo(codigoCerveja);
        ItemVenda item = new ItemVenda();
        item.setCerveja(cervejaItem);
        item.setQuantidade(2);
        item.setValorUnitario(java.math.BigDecimal.TEN);
        venda.adicionarItens(java.util.List.of(item));
        venda.setHorarioEntrega(LocalTime.of(18, 0));
        return venda;
    }
}
