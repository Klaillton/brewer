package com.algaworks.brewer.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.algaworks.brewer.model.Venda;
import com.algaworks.brewer.repository.Vendas;

class VendaRestControllerTest {

    private MockMvc mockMvc;
    private Vendas vendas;

    @BeforeEach
    void setup() {
        VendaRestController controller = new VendaRestController();
        vendas = org.mockito.Mockito.mock(Vendas.class);
        ReflectionTestUtils.setField(controller, "vendas", vendas);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void shouldReturnSaleByCodeWhenExists() throws Exception {
        Venda venda = new Venda();
        venda.setCodigo(7L);
        when(vendas.buscarComItens(7L)).thenReturn(venda);

        mockMvc.perform(get("/api/vendas/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(7));
    }

    @Test
    void shouldReturnNotFoundWhenSaleDoesNotExist() throws Exception {
        when(vendas.buscarComItens(999L)).thenReturn(null);

        mockMvc.perform(get("/api/vendas/999"))
                .andExpect(status().isNotFound());
    }
}
