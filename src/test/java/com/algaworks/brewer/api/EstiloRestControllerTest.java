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

import com.algaworks.brewer.Service.CadastroEstiloService;
import com.algaworks.brewer.model.Estilo;
import com.algaworks.brewer.repository.Estilos;

class EstiloRestControllerTest {

    private MockMvc mockMvc;
    private Estilos estilos;

    @BeforeEach
    void setup() {
        EstiloRestController controller = new EstiloRestController();
        estilos = org.mockito.Mockito.mock(Estilos.class);
        CadastroEstiloService cadastroEstiloService = org.mockito.Mockito.mock(CadastroEstiloService.class);

        ReflectionTestUtils.setField(controller, "estilos", estilos);
        ReflectionTestUtils.setField(controller, "cadastroEstiloService", cadastroEstiloService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void shouldListStylesWithPagination() throws Exception {
        Estilo estilo = new Estilo();
        estilo.setCodigo(1L);
        estilo.setNome("IPA");
        when(estilos.filtrar(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(new PageImpl<>(List.of(estilo), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/estilos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("IPA"));
    }

    @Test
    void shouldFilterStyleByName() throws Exception {
        Estilo estilo = new Estilo();
        estilo.setCodigo(2L);
        estilo.setNome("Lager");
        when(estilos.filtrar(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(new PageImpl<>(List.of(estilo), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/estilos").param("nome", "Lag"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Lager"));
    }
}
