package com.algaworks.brewer.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import com.algaworks.brewer.model.Grupo;
import com.algaworks.brewer.model.Usuario;
import com.algaworks.brewer.repository.Usuarios;

class AppUserDetailsServiceTest {

    private AppUserDetailsService service;
    private Usuarios usuarios;

    @BeforeEach
    void setup() {
        service = new AppUserDetailsService();
        usuarios = org.mockito.Mockito.mock(Usuarios.class);
        ReflectionTestUtils.setField(service, "usuarios", usuarios);
    }

    @Test
    void shouldReturnUsuarioSistemaWithUppercaseAuthorities() {
        Usuario usuario = novoUsuario();
        when(usuarios.porEmailEAtivo("usuario@brewer.com")).thenReturn(Optional.of(usuario));
        when(usuarios.permissoes(usuario)).thenReturn(List.of("cadastro_cliente", "ROLE_ADMIN"));

        UsuarioSistema usuarioSistema = (UsuarioSistema) service.loadUserByUsername("usuario@brewer.com");

        assertThat(usuarioSistema.getUsername()).isEqualTo("usuario@brewer.com");
        assertThat(usuarioSistema.getAuthorities())
                .extracting(authority -> authority.getAuthority())
                .containsExactlyInAnyOrder("CADASTRO_CLIENTE", "ROLE_ADMIN");
    }

    @Test
    void shouldThrowWhenUserIsNotFound() {
        when(usuarios.porEmailEAtivo("inexistente@brewer.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("inexistente@brewer.com"))
                .isInstanceOf(UsernameNotFoundException.class);
        verify(usuarios, never()).permissoes(org.mockito.ArgumentMatchers.any(Usuario.class));
    }

    private Usuario novoUsuario() {
        Usuario usuario = new Usuario();
        usuario.setEmail("usuario@brewer.com");
        usuario.setSenha("123");
        usuario.setNome("Usuário");
        usuario.setDataNascimento(LocalDate.now());
        usuario.setGrupos(List.of(new Grupo()));
        return usuario;
    }
}
