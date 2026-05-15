package com.algaworks.brewer.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.algaworks.brewer.Service.CadastroCervejaService;
import com.algaworks.brewer.dto.CervejaDTO;
import com.algaworks.brewer.model.Origem;
import com.algaworks.brewer.repository.Cervejas;

class CervejaRestControllerTest {

    private MockMvc mockMvc;
    private Cervejas cervejas;

    @BeforeEach
    void setup() {
        CervejaRestController controller = new CervejaRestController();
        cervejas = org.mockito.Mockito.mock(Cervejas.class);
        CadastroCervejaService cadastroCervejaService = org.mockito.Mockito.mock(CadastroCervejaService.class);

        ReflectionTestUtils.setField(controller, "cervejas", cervejas);
        ReflectionTestUtils.setField(controller, "cadastroCervejaService", cadastroCervejaService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void shouldSearchBeerBySkuOrName() throws Exception {
        CervejaDTO dto = new CervejaDTO(1L, "IPA001", "IPA", Origem.NACIONAL, BigDecimal.TEN, "ipa.png");
        when(cervejas.porSkuOuNome("ipa")).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/cervejas/search").param("skuOuNome", "ipa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("IPA"));
    }

    @Test
    void shouldReturnEmptySearchResultWhenNoBeerMatches() throws Exception {
        when(cervejas.porSkuOuNome("xpto")).thenReturn(List.of());

        mockMvc.perform(get("/api/cervejas/search").param("skuOuNome", "xpto"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
