package com.algaworks.brewer.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.algaworks.brewer.Service.RelatorioHtmlPdfService;

class RelatoriosControllerTest {

    private MockMvc mockMvc;
    private RelatorioHtmlPdfService relatorioHtmlPdfService;

    @BeforeEach
    void setup() throws Exception {
        RelatoriosController controller = new RelatoriosController();
        relatorioHtmlPdfService = mock(RelatorioHtmlPdfService.class);

        when(relatorioHtmlPdfService.gerarRelatorioVendasEmitidas(any())).thenReturn(new byte[0]);
        when(relatorioHtmlPdfService.gerarRelatorioVendasEmitidasSpike(any())).thenReturn(new byte[0]);

        ReflectionTestUtils.setField(controller, "relatorioHtmlPdfService", relatorioHtmlPdfService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void shouldReturnFormWhenDatesAreMissing() throws Exception {
        mockMvc.perform(post("/relatorios/vendasEmitidas").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("relatorio/RelatorioVendasEmitidas"))
                .andExpect(model().attributeHasFieldErrors("periodoRelatorio", "dataInicio", "dataFim"));

        verify(relatorioHtmlPdfService, never()).gerarRelatorioVendasEmitidas(any());
    }
}
