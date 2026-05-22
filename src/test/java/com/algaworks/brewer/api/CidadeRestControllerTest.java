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

import com.algaworks.brewer.Service.CadastroCidadeService;
import com.algaworks.brewer.model.Cidade;
import com.algaworks.brewer.repository.Cidades;

class CidadeRestControllerTest {

    private MockMvc mockMvc;
    private Cidades cidades;

    @BeforeEach
    void setup() {
        CidadeRestController controller = new CidadeRestController();
        cidades = org.mockito.Mockito.mock(Cidades.class);
        CadastroCidadeService cadastroCidadeService = org.mockito.Mockito.mock(CadastroCidadeService.class);

        ReflectionTestUtils.setField(controller, "cidades", cidades);
        ReflectionTestUtils.setField(controller, "cadastroCidadeService", cadastroCidadeService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void shouldListCitiesWithoutStateFilter() throws Exception {
        Cidade cidade = new Cidade();
        cidade.setCodigo(1L);
        cidade.setNome("Recife");
        when(cidades.filtrar(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(new PageImpl<>(List.of(cidade), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/cidades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Recife"));
    }

    @Test
    void shouldListCitiesByState() throws Exception {
        Cidade cidade = new Cidade();
        cidade.setCodigo(2L);
        cidade.setNome("Olinda");
        when(cidades.findByEstadoCodigo(26L)).thenReturn(List.of(cidade));

        mockMvc.perform(get("/api/cidades/estado/26"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Olinda"));
    }
}
