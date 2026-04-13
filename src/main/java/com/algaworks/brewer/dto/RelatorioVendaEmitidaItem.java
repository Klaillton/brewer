package com.algaworks.brewer.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.algaworks.brewer.model.StatusVenda;

public class RelatorioVendaEmitidaItem {

	private Long codigo;
	private LocalDateTime dataCriacao;
	private String nomeCliente;
	private String nomeUsuario;
	private BigDecimal valorTotal;
	private StatusVenda status;

	public RelatorioVendaEmitidaItem(Long codigo, LocalDateTime dataCriacao, String nomeCliente,
			String nomeUsuario, BigDecimal valorTotal, StatusVenda status) {
		this.codigo = codigo;
		this.dataCriacao = dataCriacao;
		this.nomeCliente = nomeCliente;
		this.nomeUsuario = nomeUsuario;
		this.valorTotal = valorTotal;
		this.status = status;
	}

	public Long getCodigo() {
		return codigo;
	}

	public LocalDateTime getDataCriacao() {
		return dataCriacao;
	}

	public String getNomeCliente() {
		return nomeCliente;
	}

	public String getNomeUsuario() {
		return nomeUsuario;
	}

	public BigDecimal getValorTotal() {
		return valorTotal;
	}

	public StatusVenda getStatus() {
		return status;
	}
}