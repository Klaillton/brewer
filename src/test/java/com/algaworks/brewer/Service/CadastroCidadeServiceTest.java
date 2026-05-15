package com.algaworks.brewer.Service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import jakarta.persistence.PersistenceException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.algaworks.brewer.Service.Exception.ImpossivelExcluirEntidadeException;
import com.algaworks.brewer.Service.Exception.NomeCidadeJaCadastradaException;
import com.algaworks.brewer.model.Cidade;
import com.algaworks.brewer.model.Estado;
import com.algaworks.brewer.repository.Cidades;

class CadastroCidadeServiceTest {

    private CadastroCidadeService service;
    private Cidades cidades;

    @BeforeEach
    void setup() {
        service = new CadastroCidadeService();
        cidades = org.mockito.Mockito.mock(Cidades.class);
        ReflectionTestUtils.setField(service, "cidades", cidades);
    }

    @Test
    void shouldThrowWhenSavingDuplicateCityByNameAndState() {
        Cidade cidade = new Cidade();
        cidade.setNome("Recife");
        cidade.setEstado(new Estado());
        when(cidades.findByNomeAndEstado(cidade.getNome(), cidade.getEstado())).thenReturn(Optional.of(new Cidade()));

        assertThatThrownBy(() -> service.salvar(cidade))
                .isInstanceOf(NomeCidadeJaCadastradaException.class);
    }

    @Test
    void shouldSaveCityWhenNameIsAvailableInState() {
        Cidade cidade = new Cidade();
        cidade.setNome("Olinda");
        cidade.setEstado(new Estado());
        when(cidades.findByNomeAndEstado(cidade.getNome(), cidade.getEstado())).thenReturn(Optional.empty());

        service.salvar(cidade);

        verify(cidades).save(cidade);
    }

    @Test
    void shouldTranslatePersistenceExceptionWhenDeletingCity() {
        Cidade cidade = new Cidade();
        doNothing().when(cidades).delete(cidade);
        doThrow(new PersistenceException("erro")).when(cidades).flush();

        assertThatThrownBy(() -> service.excluir(cidade))
                .isInstanceOf(ImpossivelExcluirEntidadeException.class);
    }
}
