package com.algaworks.brewer.controller;



import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import jakarta.validation.Valid;

import com.algaworks.brewer.Service.RelatorioHtmlPdfService;
import com.algaworks.brewer.dto.PeriodoRelatorio;

@Controller
@RequestMapping("/relatorios")
public class RelatoriosController {

        @Autowired
        private RelatorioHtmlPdfService relatorioHtmlPdfService;

        @GetMapping("/vendasEmitidas")
        public ModelAndView relatorioVendasEmitidas(PeriodoRelatorio periodoRelatorio) {
                ModelAndView mv = new ModelAndView("relatorio/RelatorioVendasEmitidas");
                mv.addObject("periodoRelatorio", periodoRelatorio);
                return mv;
        }

        @PostMapping("/vendasEmitidas")
        public Object gerarRelatorioVendasEmitidas(@Valid PeriodoRelatorio periodoRelatorio, BindingResult result) throws Exception {
                if (result.hasErrors()) {
                        return relatorioVendasEmitidas(periodoRelatorio);
                }

                byte[] relatorio = relatorioHtmlPdfService.gerarRelatorioVendasEmitidas(periodoRelatorio);

                return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                                .header("Content-Disposition", "inline; filename=relatorio-vendas-emitidas.pdf")
                                .body(relatorio);
        }

        @PostMapping("/vendasEmitidas/spike-html")
        public Object gerarRelatorioVendasEmitidasSpike(@Valid PeriodoRelatorio periodoRelatorio, BindingResult result) throws Exception {
                if (result.hasErrors()) {
                        return relatorioVendasEmitidas(periodoRelatorio);
                }

                byte[] relatorio = relatorioHtmlPdfService.gerarRelatorioVendasEmitidasSpike(periodoRelatorio);

                return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                                .header("Content-Disposition", "inline; filename=relatorio-vendas-emitidas-spike.pdf")
                                .body(relatorio);
        }

}
