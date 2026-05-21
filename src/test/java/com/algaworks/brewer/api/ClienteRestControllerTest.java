package com.algaworks.brewer.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.algaworks.brewer.Service.CadastroClienteService;
import com.algaworks.brewer.model.Cliente;
import com.algaworks.brewer.model.TipoPessoa;
import com.algaworks.brewer.repository.Clientes;

class ClienteRestControllerTest {

    private MockMvc mockMvc;
    private Clientes clientes;

    @BeforeEach
    void setup() {
        ClienteRestController controller = new ClienteRestController();
        clientes = org.mockito.Mockito.mock(Clientes.class);
        CadastroClienteService cadastroClienteService = org.mockito.Mockito.mock(CadastroClienteService.class);

        ReflectionTestUtils.setField(controller, "clientes", clientes);
        ReflectionTestUtils.setField(controller, "cadastroClienteService", cadastroClienteService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void shouldSearchCustomerByName() throws Exception {
        Cliente cliente = new Cliente();
        cliente.setCodigo(1L);
        cliente.setNome("João");
        cliente.setTipoPessoa(TipoPessoa.FISICA);
        cliente.setCpfOuCnpj("12345678900");
        when(clientes.filtrar(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(new PageImpl<>(List.of(cliente), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/clientes").param("nome", "Jo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("João"));
    }

    @Test
    void shouldSearchCustomerByCpfCnpj() throws Exception {
        Cliente cliente = new Cliente();
        cliente.setCodigo(2L);
        cliente.setNome("Maria");
        cliente.setTipoPessoa(TipoPessoa.FISICA);
        cliente.setCpfOuCnpj("12345678900");
        when(clientes.filtrar(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(new PageImpl<>(List.of(cliente), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/clientes").param("cpfOuCnpj", "12345678900"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Maria"));
    }
}
