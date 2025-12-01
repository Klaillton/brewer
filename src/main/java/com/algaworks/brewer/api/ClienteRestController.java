package com.algaworks.brewer.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.algaworks.brewer.Service.CadastroClienteService;
import com.algaworks.brewer.model.Cliente;
import com.algaworks.brewer.repository.Clientes;
import com.algaworks.brewer.repository.filter.ClienteFilter;

import jakarta.validation.Valid;

/**
 * REST API Controller for Clientes (Customers)
 */
@RestController
@RequestMapping("/api/clientes")
public class ClienteRestController {

    @Autowired
    private Clientes clientes;

    @Autowired
    private CadastroClienteService cadastroClienteService;

    /**
     * List all clientes with pagination and filtering
     */
    @GetMapping
    public ResponseEntity<Page<Cliente>> listar(ClienteFilter filtro, Pageable pageable) {
        Page<Cliente> page = clientes.filtrar(filtro, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * Get a cliente by ID
     */
    @GetMapping("/{codigo}")
    public ResponseEntity<Cliente> buscar(@PathVariable Long codigo) {
        Cliente cliente = clientes.buscarEstadoCidade(codigo);
        if (cliente != null) {
            return ResponseEntity.ok(cliente);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Create a new cliente
     */
    @PostMapping
    public ResponseEntity<Cliente> criar(@Valid @RequestBody Cliente cliente) {
        cadastroClienteService.salvar(cliente);
        return ResponseEntity.status(HttpStatus.CREATED).body(cliente);
    }

    /**
     * Update an existing cliente
     */
    @PutMapping("/{codigo}")
    public ResponseEntity<Cliente> atualizar(@PathVariable Long codigo, @Valid @RequestBody Cliente cliente) {
        if (!clientes.existsById(codigo)) {
            return ResponseEntity.notFound().build();
        }
        cliente.setCodigo(codigo);
        cadastroClienteService.salvar(cliente);
        return ResponseEntity.ok(cliente);
    }

    /**
     * Delete a cliente
     */
    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> excluir(@PathVariable Long codigo) {
        if (!clientes.existsById(codigo)) {
            return ResponseEntity.notFound().build();
        }
        clientes.deleteById(codigo);
        return ResponseEntity.noContent().build();
    }

}
