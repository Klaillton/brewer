package com.algaworks.brewer.Service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import jakarta.persistence.PersistenceException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.algaworks.brewer.Service.Exception.ImpossivelExcluirEntidadeException;
import com.algaworks.brewer.model.Cerveja;
import com.algaworks.brewer.repository.Cervejas;
import com.algaworks.brewer.storage.FotoStorage;

class CadastroCervejaServiceTest {

    private CadastroCervejaService service;
    private Cervejas cervejas;
    private FotoStorage fotoStorage;

    @BeforeEach
    void setup() {
        service = new CadastroCervejaService();
        cervejas = org.mockito.Mockito.mock(Cervejas.class);
        fotoStorage = org.mockito.Mockito.mock(FotoStorage.class);
        ReflectionTestUtils.setField(service, "cervejas", cervejas);
        ReflectionTestUtils.setField(service, "fotoStorage", fotoStorage);
    }

    @Test
    void shouldSaveBeer() {
        Cerveja cerveja = new Cerveja();

        service.salvar(cerveja);

        verify(cervejas).save(cerveja);
    }

    @Test
    void shouldDeleteBeerAndStoredPhoto() {
        Cerveja cerveja = new Cerveja();
        cerveja.setFoto("foto.png");
        doNothing().when(cervejas).delete(cerveja);
        doNothing().when(cervejas).flush();

        service.excluir(cerveja);

        verify(cervejas).delete(cerveja);
        verify(cervejas).flush();
        verify(fotoStorage).excluir("foto.png");
    }

    @Test
    void shouldTranslatePersistenceExceptionWhenDeletingBeer() {
        Cerveja cerveja = new Cerveja();
        cerveja.setFoto("foto.png");
        doNothing().when(cervejas).delete(cerveja);
        doThrow(new PersistenceException("erro")).when(cervejas).flush();

        assertThatThrownBy(() -> service.excluir(cerveja))
                .isInstanceOf(ImpossivelExcluirEntidadeException.class);
        verify(fotoStorage, never()).excluir("foto.png");
    }
}
