package com.algaworks.brewer.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.algaworks.brewer.Service.CadastroUsuarioService;
import com.algaworks.brewer.Service.StatusUsuario;
import com.algaworks.brewer.model.Usuario;
import com.algaworks.brewer.repository.Usuarios;
import com.algaworks.brewer.repository.filter.UsuarioFilter;

import jakarta.validation.Valid;

/**
 * REST API Controller for Usuarios (Users)
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioRestController {

    @Autowired
    private Usuarios usuarios;

    @Autowired
    private CadastroUsuarioService cadastroUsuarioService;

    /**
     * List all usuarios with pagination and filtering
     */
    @GetMapping
    public ResponseEntity<Page<Usuario>> listar(UsuarioFilter filtro, Pageable pageable) {
        Page<Usuario> page = usuarios.filtrar(filtro, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * Get a usuario by ID
     */
    @GetMapping("/{codigo}")
    public ResponseEntity<Usuario> buscar(@PathVariable Long codigo) {
        Usuario usuario = usuarios.buscarComGrupos(codigo);
        if (usuario != null) {
            return ResponseEntity.ok(usuario);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Create a new usuario
     */
    @PostMapping
    public ResponseEntity<Usuario> criar(@Valid @RequestBody Usuario usuario) {
        cadastroUsuarioService.salvar(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
    }

    /**
     * Update an existing usuario
     */
    @PutMapping("/{codigo}")
    public ResponseEntity<Usuario> atualizar(@PathVariable Long codigo, @Valid @RequestBody Usuario usuario) {
        if (!usuarios.existsById(codigo)) {
            return ResponseEntity.notFound().build();
        }
        usuario.setCodigo(codigo);
        cadastroUsuarioService.salvar(usuario);
        return ResponseEntity.ok(usuario);
    }

    /**
     * Update usuario status (activate/deactivate)
     */
    @PutMapping("/{codigo}/status")
    public ResponseEntity<Void> atualizarStatus(@PathVariable Long codigo, @RequestBody StatusUsuario status) {
        cadastroUsuarioService.alterarStatus(new Long[] { codigo }, status);
        return ResponseEntity.ok().build();
    }

}
