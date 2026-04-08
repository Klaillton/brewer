package com.algaworks.brewer.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.algaworks.brewer.dto.RelatorioVendaEmitidaItem;
import com.algaworks.brewer.model.StatusVenda;
import com.algaworks.brewer.model.Venda;
import com.algaworks.brewer.repository.helper.venda.VendasQueries;

public interface Vendas extends JpaRepository<Venda, Long>, VendasQueries{

	@Query("select new com.algaworks.brewer.dto.RelatorioVendaEmitidaItem(" +
			"v.codigo, v.dataCriacao, cliente.nome, usuario.nome, v.valorTotal, v.status) " +
			"from Venda v join v.cliente cliente join v.usuario usuario " +
			"where v.status = :status and v.dataCriacao between :dataInicio and :dataFim " +
			"order by v.dataCriacao asc, v.codigo asc")
	List<RelatorioVendaEmitidaItem> buscarEmitidasNoPeriodo(@Param("status") StatusVenda status,
			@Param("dataInicio") LocalDateTime dataInicio, @Param("dataFim") LocalDateTime dataFim);

}
