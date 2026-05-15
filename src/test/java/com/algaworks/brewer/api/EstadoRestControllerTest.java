package com.algaworks.brewer.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.algaworks.brewer.model.Estado;
import com.algaworks.brewer.repository.Estados;

class EstadoRestControllerTest {

    private MockMvc mockMvc;
    private Estados estados;

    @BeforeEach
    void setup() {
        EstadoRestController controller = new EstadoRestController();
        estados = org.mockito.Mockito.mock(Estados.class);
        ReflectionTestUtils.setField(controller, "estados", estados);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void shouldListStates() throws Exception {
        Estado sp = new Estado();
        sp.setCodigo(1L);
        sp.setSigla("SP");
        when(estados.findAll()).thenReturn(List.of(sp));

        mockMvc.perform(get("/api/estados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sigla").value("SP"));
    }

    @Test
    void shouldReturnEmptyListWhenNoStatesExist() throws Exception {
        when(estados.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/estados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
