package com.algaworks.brewer.Service;

import java.util.Optional;

import javax.persistence.PersistenceException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.algaworks.brewer.Service.Exception.ImpossivelExcluirEntidadeException;
import com.algaworks.brewer.Service.Exception.SenhaObrigatoriaUsuarioException;
import com.algaworks.brewer.Service.Exception.UsuarioJaCadastradoException;
import com.algaworks.brewer.model.Usuario;
import com.algaworks.brewer.repository.Usuarios;

@Service
public class CadastroUsuarioService {

	@Autowired
	private Usuarios usuarios;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Transactional
	public void salvar(Usuario usuario) {
		Optional<Usuario> usuarioExistente = usuarios.findByEmailOrCodigo(usuario.getEmail(), usuario.getCodigo());
		
		if (usuarioExistente.isPresent() && !usuarioExistente.get().equals(usuario)) {
			throw new UsuarioJaCadastradoException("E-mail já cadastrado");
		}

		if (usuario.isNovo() && StringUtils.isEmpty(usuario.getSenha())) {
			throw new SenhaObrigatoriaUsuarioException("Senha é obrigatória para novo usuário!");
		}

		validarEncodarSenha(usuario, usuarioExistente);

		usuario.setConfirmacaoSenha(usuario.getSenha());
		
		validarStatus(usuario, usuarioExistente);

		usuarios.save(usuario);
	}

	private void validarStatus(Usuario usuario, Optional<Usuario> usuarioExistente) {
		if(!usuario.isNovo() && usuario.getAtivo() == null) {
			usuario.setAtivo(usuarioExistente.get().getAtivo());
		}
	}

	private void validarEncodarSenha(Usuario usuario, Optional<Usuario> usuarioExistente) {
		if (usuario.isNovo() || !StringUtils.isEmpty(usuario.getSenha())) {
			usuario.setSenha(this.passwordEncoder.encode(usuario.getSenha()));
		} else if (StringUtils.isEmpty(usuario.getSenha())) {
			usuario.setSenha(usuarioExistente.get().getSenha());
		}
	}

	@Transactional
	public void alterarStatus(Long[] codigos, StatusUsuario statusUsuario) {
		statusUsuario.executar(codigos, usuarios);

	}

	@Transactional
	public void excluir(Usuario usuario) {
		try {
			usuarios.delete(usuario);
			usuarios.flush();
		} catch (PersistenceException e) {
			throw new ImpossivelExcluirEntidadeException("Impossivel apagar usuário. Já foi utilizada em alguma venda.");
		}
		
	}

}
