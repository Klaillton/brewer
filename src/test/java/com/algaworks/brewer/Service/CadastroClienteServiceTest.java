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

import com.algaworks.brewer.Service.Exception.CpfCnpjClienteJaCadastradoException;
import com.algaworks.brewer.Service.Exception.ImpossivelExcluirEntidadeException;
import com.algaworks.brewer.model.Cliente;
import com.algaworks.brewer.model.TipoPessoa;
import com.algaworks.brewer.repository.Clientes;

class CadastroClienteServiceTest {

    private CadastroClienteService service;
    private Clientes clientes;

    @BeforeEach
    void setup() {
        service = new CadastroClienteService();
        clientes = org.mockito.Mockito.mock(Clientes.class);
        ReflectionTestUtils.setField(service, "clientes", clientes);
    }

    @Test
    void shouldThrowWhenSavingNewClientWithDuplicatedCpfCnpj() {
        Cliente cliente = novoCliente("123.456.789-00");
        when(clientes.findByCpfOuCnpj(cliente.getCpfOuCnpjSemFormatacao())).thenReturn(Optional.of(new Cliente()));

        assertThatThrownBy(() -> service.salvar(cliente))
                .isInstanceOf(CpfCnpjClienteJaCadastradoException.class);
    }

    @Test
    void shouldAllowExistingClientWithDuplicatedCpfCnpj() {
        Cliente cliente = novoCliente("123.456.789-00");
        cliente.setCodigo(1L);
        when(clientes.findByCpfOuCnpj(cliente.getCpfOuCnpjSemFormatacao())).thenReturn(Optional.of(new Cliente()));

        service.salvar(cliente);

        verify(clientes).save(cliente);
    }

    @Test
    void shouldTranslatePersistenceExceptionWhenDeletingClient() {
        Cliente cliente = novoCliente("123.456.789-00");
        doNothing().when(clientes).delete(cliente);
        doThrow(new PersistenceException("erro")).when(clientes).flush();

        assertThatThrownBy(() -> service.excluir(cliente))
                .isInstanceOf(ImpossivelExcluirEntidadeException.class);
    }

    private Cliente novoCliente(String cpf) {
        Cliente cliente = new Cliente();
        cliente.setNome("Cliente");
        cliente.setTipoPessoa(TipoPessoa.FISICA);
        cliente.setCpfOuCnpj(cpf);
        return cliente;
    }
}
