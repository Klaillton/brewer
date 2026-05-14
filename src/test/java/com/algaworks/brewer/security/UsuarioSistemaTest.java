package com.algaworks.brewer.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.algaworks.brewer.model.Usuario;

class UsuarioSistemaTest {

    @Test
    void shouldWrapUsuarioDataIntoSpringSecurityUser() {
        Usuario usuario = new Usuario();
        usuario.setEmail("admin@brewer.com");
        usuario.setSenha("senha-criptografada");
        usuario.setDataNascimento(LocalDate.of(1990, 1, 1));

        UsuarioSistema usuarioSistema = new UsuarioSistema(usuario, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        assertThat(usuarioSistema.getUsername()).isEqualTo("admin@brewer.com");
        assertThat(usuarioSistema.getPassword()).isEqualTo("senha-criptografada");
        assertThat(usuarioSistema.getAuthorities()).extracting("authority").containsExactly("ROLE_ADMIN");
        assertThat(usuarioSistema.getUsuario()).isSameAs(usuario);
    }
}
