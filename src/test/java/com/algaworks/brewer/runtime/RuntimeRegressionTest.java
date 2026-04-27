package com.algaworks.brewer.runtime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class RuntimeRegressionTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void estadosApiShouldReturnSeededStates() throws Exception {
        mockMvc.perform(get("/api/estados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").exists())
                .andExpect(jsonPath("$[?(@.sigla == 'SP')]").exists());
    }

    @Test
    @WithMockUser(username = "admin@brewer.com", roles = { "CADASTRAR_CIDADE" })
    void cidadesPageShouldRenderSuccessfully() throws Exception {
        mockMvc.perform(get("/cidades"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin@brewer.com", roles = { "CADASTRAR_CIDADE" })
    void dashboardStatsByMonthEndpointShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/vendas/stats/por-mes"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin@brewer.com", roles = { "CADASTRAR_CIDADE" })
    void vendasPageShouldAcceptSortingByNestedFields() throws Exception {
        mockMvc.perform(get("/vendas")
                        .param("sort", "cliente.nome,asc")
                        .param("sort", "usuario.nome,desc"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin@brewer.com", roles = { "CADASTRAR_CIDADE" })
    void vendasApiShouldAcceptLegacyAndCurrentSortFields() throws Exception {
        mockMvc.perform(get("/api/vendas")
                        .param("sort", "c.nome,asc"))
                .andExpect(status().isOk());
    }
}
