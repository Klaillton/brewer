package com.algaworks.brewer.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.algaworks.brewer.model.Usuario;
import com.algaworks.brewer.repository.filter.UsuarioFilter;
import com.algaworks.brewer.repository.helper.usuario.UsuariosQueries;

public interface Usuarios extends JpaRepository<Usuario, Long>, UsuariosQueries {	

	public List<Usuario> findByCodigoIn(Long[] codigos);

	@Override
	public Page<Usuario> filtrar(UsuarioFilter usuarioFilter, Pageable pageable);

	public Optional<Usuario> findByEmailOrCodigo(String email, Long codigo);
	

}
