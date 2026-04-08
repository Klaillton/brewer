package com.algaworks.brewer.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.algaworks.brewer.dto.PeriodoRelatorio;
import com.algaworks.brewer.dto.RelatorioVendaEmitidaItem;
import com.algaworks.brewer.model.StatusVenda;
import com.algaworks.brewer.repository.Vendas;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

@Service
public class RelatorioHtmlPdfService {

	private final Vendas vendas;
	private final TemplateEngine templateEngine;

	public RelatorioHtmlPdfService(Vendas vendas, TemplateEngine templateEngine) {
		this.vendas = vendas;
		this.templateEngine = templateEngine;
	}

	@Transactional(readOnly = true)
	public byte[] gerarRelatorioVendasEmitidasSpike(PeriodoRelatorio periodoRelatorio) throws Exception {
		LocalDateTime dataInicio = LocalDateTime.of(periodoRelatorio.getDataInicio(), LocalTime.of(0, 0, 0));
		LocalDateTime dataFim = LocalDateTime.of(periodoRelatorio.getDataFim(), LocalTime.of(23, 59, 59));

		List<RelatorioVendaEmitidaItem> vendasEmitidas = vendas.buscarEmitidasNoPeriodo(StatusVenda.EMITIDA, dataInicio,
				dataFim);
		BigDecimal valorTotal = vendasEmitidas.stream().map(RelatorioVendaEmitidaItem::getValorTotal)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		Context context = new Context();
		context.setVariable("periodoRelatorio", periodoRelatorio);
		context.setVariable("vendasEmitidas", vendasEmitidas);
		context.setVariable("valorTotal", valorTotal);
		context.setVariable("quantidadeVendas", vendasEmitidas.size());
		context.setVariable("dataGeracao", LocalDateTime.now());

		String html = templateEngine.process("relatorio/RelatorioVendasEmitidasSpikePdf", context);

		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			PdfRendererBuilder builder = new PdfRendererBuilder();
			builder.useFastMode();
			builder.withHtmlContent(html, null);
			builder.toStream(outputStream);
			builder.run();
			return outputStream.toByteArray();
		}
	}
}