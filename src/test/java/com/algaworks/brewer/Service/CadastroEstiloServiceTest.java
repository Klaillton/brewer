package com.algaworks.brewer.Service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import jakarta.persistence.PersistenceException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.algaworks.brewer.Service.Exception.ImpossivelExcluirEntidadeException;
import com.algaworks.brewer.Service.Exception.NomeEstiloJaCadastradoException;
import com.algaworks.brewer.model.Estilo;
import com.algaworks.brewer.repository.Estilos;

class CadastroEstiloServiceTest {

    private CadastroEstiloService service;
    private Estilos estilos;

    @BeforeEach
    void setup() {
        service = new CadastroEstiloService();
        estilos = org.mockito.Mockito.mock(Estilos.class);
        ReflectionTestUtils.setField(service, "estilos", estilos);
    }

    @Test
    void shouldThrowWhenSavingDuplicateName() {
        Estilo estilo = new Estilo();
        estilo.setNome("IPA");
        when(estilos.findByNomeIgnoreCase("IPA")).thenReturn(Optional.of(new Estilo()));

        assertThatThrownBy(() -> service.salvar(estilo))
                .isInstanceOf(NomeEstiloJaCadastradoException.class);

        verify(estilos, never()).saveAndFlush(estilo);
    }

    @Test
    void shouldSaveAndReturnStyleWhenNameIsNew() {
        Estilo estilo = new Estilo();
        estilo.setNome("Lager");
        when(estilos.findByNomeIgnoreCase("Lager")).thenReturn(Optional.empty());
        when(estilos.saveAndFlush(estilo)).thenReturn(estilo);

        Estilo salvo = service.salvar(estilo);

        assertThat(salvo).isSameAs(estilo);
        verify(estilos).saveAndFlush(estilo);
    }

    @Test
    void shouldTranslatePersistenceExceptionWhenDeleting() {
        Estilo estilo = new Estilo();
        doNothing().when(estilos).delete(estilo);
        doThrow(new PersistenceException("erro")).when(estilos).flush();

        assertThatThrownBy(() -> service.excluir(estilo))
                .isInstanceOf(ImpossivelExcluirEntidadeException.class);
    }
}
