package com.algaworks.brewer.Service;

import java.util.Optional;

import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.algaworks.brewer.Service.Exception.CpfCnpjClienteJaCadastradoException;
import com.algaworks.brewer.Service.Exception.ImpossivelExcluirEntidadeException;
import com.algaworks.brewer.model.Cliente;
import com.algaworks.brewer.repository.Clientes;

@Service
public class CadastroClienteService {

	@Autowired
	private Clientes clientes;
	
	@Transactional
	public void salvar(Cliente cliente) {
		Optional<Cliente> clienteExistente = clientes.findByCpfOuCnpj(cliente.getCpfOuCnpjSemFormatacao());
		if (clienteExistente.isPresent() && cliente.getCodigo() == null) {
			throw new CpfCnpjClienteJaCadastradoException("CPF/CNPJ já cadastrado");
		}
		
		clientes.save(cliente);
	}

	@Transactional
	public void excluir(Cliente cliente) {
		try {
			clientes.delete(cliente);
			clientes.flush();
		} catch (PersistenceException e) {
			throw new ImpossivelExcluirEntidadeException("Impossivel apagar cliente. Já foi utilizado em alguma venda.");
		}		
	}
	
}
