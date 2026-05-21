package com.algaworks.brewer.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.algaworks.brewer.Service.CadastroUsuarioService;
import com.algaworks.brewer.model.Usuario;
import com.algaworks.brewer.repository.Usuarios;

class UsuarioRestControllerTest {

    private MockMvc mockMvc;
    private Usuarios usuarios;

    @BeforeEach
    void setup() {
        UsuarioRestController controller = new UsuarioRestController();
        usuarios = org.mockito.Mockito.mock(Usuarios.class);
        CadastroUsuarioService cadastroUsuarioService = org.mockito.Mockito.mock(CadastroUsuarioService.class);

        ReflectionTestUtils.setField(controller, "usuarios", usuarios);
        ReflectionTestUtils.setField(controller, "cadastroUsuarioService", cadastroUsuarioService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void shouldListUsers() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setCodigo(1L);
        usuario.setNome("Admin");
        when(usuarios.filtrar(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(new PageImpl<>(List.of(usuario), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Admin"));
    }

    @Test
    void shouldAllowListingUsersWithoutAuthentication() throws Exception {
        when(usuarios.filtrar(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }
}
