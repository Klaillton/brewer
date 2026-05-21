package com.algaworks.brewer.Service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.PersistenceException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.algaworks.brewer.Service.Exception.ImpossivelExcluirEntidadeException;
import com.algaworks.brewer.Service.Exception.SenhaObrigatoriaUsuarioException;
import com.algaworks.brewer.Service.Exception.UsuarioJaCadastradoException;
import com.algaworks.brewer.model.Grupo;
import com.algaworks.brewer.model.Usuario;
import com.algaworks.brewer.repository.Usuarios;

class CadastroUsuarioServiceTest {

    private CadastroUsuarioService service;
    private Usuarios usuarios;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        service = new CadastroUsuarioService();
        usuarios = org.mockito.Mockito.mock(Usuarios.class);
        passwordEncoder = org.mockito.Mockito.mock(PasswordEncoder.class);
        ReflectionTestUtils.setField(service, "usuarios", usuarios);
        ReflectionTestUtils.setField(service, "passwordEncoder", passwordEncoder);
    }

    @Test
    void shouldThrowWhenSavingUserWithDuplicateEmail() {
        Usuario usuario = novoUsuario();
        Usuario existente = novoUsuario();
        existente.setCodigo(10L);

        when(usuarios.findByEmailOrCodigo(usuario.getEmail(), usuario.getCodigo())).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.salvar(usuario))
                .isInstanceOf(UsuarioJaCadastradoException.class);
    }

    @Test
    void shouldThrowWhenSavingNewUserWithoutPassword() {
        Usuario usuario = novoUsuario();
        usuario.setSenha("");
        when(usuarios.findByEmailOrCodigo(usuario.getEmail(), usuario.getCodigo())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.salvar(usuario))
                .isInstanceOf(SenhaObrigatoriaUsuarioException.class);
    }

    @Test
    void shouldEncodePasswordAndSaveNewUser() {
        Usuario usuario = novoUsuario();
        usuario.setSenha("senha123");
        when(usuarios.findByEmailOrCodigo(usuario.getEmail(), usuario.getCodigo())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("senha123")).thenReturn("encoded");

        service.salvar(usuario);

        assertThat(usuario.getSenha()).isEqualTo("encoded");
        assertThat(usuario.getConfirmacaoSenha()).isEqualTo("encoded");
        verify(usuarios).save(usuario);
    }

    @Test
    void shouldKeepStoredPasswordAndActiveStatusWhenUpdatingWithoutPassword() {
        Usuario usuario = novoUsuario();
        usuario.setCodigo(5L);
        usuario.setSenha("");
        usuario.setAtivo(null);

        Usuario existente = novoUsuario();
        existente.setCodigo(5L);
        existente.setSenha("senha-antiga");
        existente.setAtivo(true);

        when(usuarios.findByEmailOrCodigo(usuario.getEmail(), usuario.getCodigo())).thenReturn(Optional.of(existente));

        service.salvar(usuario);

        assertThat(usuario.getSenha()).isEqualTo("senha-antiga");
        assertThat(usuario.getConfirmacaoSenha()).isEqualTo("senha-antiga");
        assertThat(usuario.getAtivo()).isTrue();
        verifyNoInteractions(passwordEncoder);
        verify(usuarios).save(usuario);
    }

    @Test
    void shouldDelegateStatusChangeExecution() {
        Long[] codigos = new Long[] { 1L };
        Usuario usuario = novoUsuario();
        usuario.setAtivo(false);
        when(usuarios.findByCodigoIn(codigos)).thenReturn(List.of(usuario));

        service.alterarStatus(codigos, StatusUsuario.ATIVAR);

        assertThat(usuario.getAtivo()).isTrue();
    }

    @Test
    void shouldTranslatePersistenceExceptionWhenDeletingUser() {
        Usuario usuario = novoUsuario();
        doNothing().when(usuarios).delete(usuario);
        doThrow(new PersistenceException("erro")).when(usuarios).flush();

        assertThatThrownBy(() -> service.excluir(usuario))
                .isInstanceOf(ImpossivelExcluirEntidadeException.class);
    }

    private Usuario novoUsuario() {
        Usuario usuario = new Usuario();
        usuario.setNome("Usuário");
        usuario.setEmail("usuario@brewer.com");
        usuario.setDataNascimento(LocalDate.now());
        usuario.setGrupos(List.of(new Grupo()));
        return usuario;
    }
}
