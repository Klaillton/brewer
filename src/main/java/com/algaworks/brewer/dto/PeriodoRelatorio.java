package com.algaworks.brewer.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public class PeriodoRelatorio {
	
	@NotNull(message = "{periodoRelatorio.dataInicio.required}")
	@DateTimeFormat(pattern = "dd/MM/yyyy")
	private LocalDate dataInicio;
	
	@NotNull(message = "{periodoRelatorio.dataFim.required}")
	@DateTimeFormat(pattern = "dd/MM/yyyy")
	private LocalDate dataFim;
	
	public LocalDate getDataInicio() {
		return dataInicio;
	}
	public void setDataInicio(LocalDate dataInicio) {
		this.dataInicio = dataInicio;
	}
	public LocalDate getDataFim() {
		return dataFim;
	}
	public void setDataFim(LocalDate dataFim) {
		this.dataFim = dataFim;
	}
	
	@AssertTrue(message = "{periodoRelatorio.periodo.invalido}")
	public boolean isPeriodoValido() {
		if (dataInicio == null || dataFim == null) {
			return true;
		}
		
		return !dataFim.isBefore(dataInicio);
	}

}
