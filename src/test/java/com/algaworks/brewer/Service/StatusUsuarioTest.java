package com.algaworks.brewer.Service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.algaworks.brewer.model.Usuario;
import com.algaworks.brewer.repository.Usuarios;

class StatusUsuarioTest {

    @Test
    void shouldActivateUsersWhenStatusIsAtivar() {
        Usuario usuario1 = new Usuario();
        usuario1.setAtivo(false);

        Usuario usuario2 = new Usuario();
        usuario2.setAtivo(false);

        Usuarios usuarios = org.mockito.Mockito.mock(Usuarios.class);
        when(usuarios.findByCodigoIn(any(Long[].class))).thenReturn(List.of(usuario1, usuario2));

        StatusUsuario.ATIVAR.executar(new Long[] { 1L, 2L }, usuarios);

        assertThat(usuario1.getAtivo()).isTrue();
        assertThat(usuario2.getAtivo()).isTrue();
    }

    @Test
    void shouldDeactivateUsersWhenStatusIsDesativar() {
        Usuario usuario1 = new Usuario();
        usuario1.setAtivo(true);

        Usuario usuario2 = new Usuario();
        usuario2.setAtivo(true);

        Usuarios usuarios = org.mockito.Mockito.mock(Usuarios.class);
        when(usuarios.findByCodigoIn(any(Long[].class))).thenReturn(List.of(usuario1, usuario2));

        StatusUsuario.DESATIVAR.executar(new Long[] { 1L, 2L }, usuarios);

        assertThat(usuario1.getAtivo()).isFalse();
        assertThat(usuario2.getAtivo()).isFalse();
    }
}
