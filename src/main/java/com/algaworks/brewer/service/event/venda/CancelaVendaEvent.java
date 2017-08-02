package com.algaworks.brewer.service.event.venda;

import com.algaworks.brewer.model.Venda;

public class CancelaVendaEvent {
	
	private Venda venda;

	public CancelaVendaEvent(Venda venda) {
		this.venda = venda;
	}

	public Venda getVenda() {
		return venda;
	}

}
