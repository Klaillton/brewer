package com.algaworks.brewer.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.algaworks.brewer.Service.event.venda.CancelaVendaEvent;
import com.algaworks.brewer.Service.event.venda.VendaEvent;
import com.algaworks.brewer.model.StatusVenda;
import com.algaworks.brewer.model.Venda;
import com.algaworks.brewer.repository.Vendas;

@Service
public class CadastroVendaService {

	@Autowired
	private Vendas vendas;
	
	@Autowired
	private ApplicationEventPublisher publisher;
	
	
	@Transactional
	public Venda salvar(Venda venda) {
		if(venda.isSalvarProibido()) {
			throw new RuntimeException("Usuário tentando salvar uma venda proibida!");
		}
		
		if(venda.isNova()) {
			venda.setDataCriacao(LocalDateTime.now());
		} else {
			Venda vendaExistente = vendas.findOne(venda.getCodigo());
			venda.setDataCriacao(vendaExistente.getDataCriacao());
		}
		
		
		if(venda.getDataEntrega() != null) {
			venda.setDataHoraEntrega(LocalDateTime.of(venda.getDataEntrega()
					, venda.getHorarioEntrega() != null ? venda.getHorarioEntrega() : LocalTime.NOON));
		}
		
//		if(temEstoque(venda)) {
//			throw new RuntimeException("Não há itens suficientes no estoque!");/*Não pode ser o RunTimeException 
//			tem de verificar um metodo de retornar msg para o usuario na tela*/
//		}
		
		return vendas.saveAndFlush(venda); /*o metodo saveAndFlush ao realizar o salvamento no BD, 
		retorna o resultado para o objeto que chama o metodo*/
	}
	
//	private boolean temEstoque(Venda venda) {/*Verificar de acordo com as regras de negócio se a venda pode ser feita sem haver os itens no estoque*/
//		
//		boolean temEstoque = false;
//		
//		for (ItemVenda item: venda.getItens()) {
//			Cerveja cerveja = cervejas.findOne(item.getCerveja().getCodigo());
//			temEstoque = (cerveja.getQuantidadeEstoque() < item.getQuantidade()) ? true : false;
//		}
//		
//		return temEstoque;
//	}
	

	@Transactional
	public void emitir(Venda venda) {
		
//		if(temEstoque(venda)) {
//			throw new RuntimeException("Não há itens suficientes no estoque!");
//		}
		
		venda.setStatus(StatusVenda.EMITIDA);
		salvar(venda);
		
		publisher.publishEvent(new VendaEvent(venda));
	}

	@PreAuthorize("#venda.usuario == principal.usuario or hasRole('CANCELAR_VENDA')")
	@Transactional
	public void cancelar(Venda venda) {
		
		Venda vendaExistente = vendas.findOne(venda.getCodigo());
		
		vendaExistente.setStatus(StatusVenda.CANCELADA);
		vendas.save(vendaExistente);
		
		if(venda.getStatus() == StatusVenda.EMITIDA) {
			publisher.publishEvent(new CancelaVendaEvent(vendaExistente));			
		}
	
	}

	

	
}
