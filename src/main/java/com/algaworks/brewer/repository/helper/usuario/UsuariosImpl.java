package com.algaworks.brewer.repository.helper.usuario;

import java.util.Optional;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;

import com.algaworks.brewer.model.Usuario;

public class UsuariosImpl implements UsuariosQueries{

	@Autowired
	private EntityManager manager;
	
	@Override
	public Optional<Usuario> porEmailEAtivo(String email) {
		return manager
					.createQuery("from Usuario where lower(email) = lower(:email) and ativo = true", Usuario.class)
					.setParameter("email", email).getResultList().stream().findFirst();
	}
	
	

}
