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
import org.springframework.web.bind.annotation.RestController;

import com.algaworks.brewer.Service.CadastroEstiloService;
import com.algaworks.brewer.model.Estilo;
import com.algaworks.brewer.repository.Estilos;
import com.algaworks.brewer.repository.filter.EstiloFilter;

import jakarta.validation.Valid;

/**
 * REST API Controller for Estilos (Beer Styles)
 */
@RestController
@RequestMapping("/api/estilos")
public class EstiloRestController {

    @Autowired
    private Estilos estilos;

    @Autowired
    private CadastroEstiloService cadastroEstiloService;

    /**
     * List all estilos with pagination and filtering
     */
    @GetMapping
    public ResponseEntity<Page<Estilo>> listar(EstiloFilter filtro, Pageable pageable) {
        Page<Estilo> page = estilos.filtrar(filtro, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * Get all estilos without pagination
     */
    @GetMapping("/all")
    public ResponseEntity<List<Estilo>> listarTodos() {
        return ResponseEntity.ok(estilos.findAll());
    }

    /**
     * Get an estilo by ID
     */
    @GetMapping("/{codigo}")
    public ResponseEntity<Estilo> buscar(@PathVariable Long codigo) {
        return estilos.findById(codigo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new estilo
     */
    @PostMapping
    public ResponseEntity<Estilo> criar(@Valid @RequestBody Estilo estilo) {
        Estilo estiloSalvo = cadastroEstiloService.salvar(estilo);
        return ResponseEntity.status(HttpStatus.CREATED).body(estiloSalvo);
    }

    /**
     * Update an existing estilo
     */
    @PutMapping("/{codigo}")
    public ResponseEntity<Estilo> atualizar(@PathVariable Long codigo, @Valid @RequestBody Estilo estilo) {
        if (!estilos.existsById(codigo)) {
            return ResponseEntity.notFound().build();
        }
        estilo.setCodigo(codigo);
        Estilo estiloAtualizado = cadastroEstiloService.salvar(estilo);
        return ResponseEntity.ok(estiloAtualizado);
    }

    /**
     * Delete an estilo
     */
    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> excluir(@PathVariable Long codigo) {
        if (!estilos.existsById(codigo)) {
            return ResponseEntity.notFound().build();
        }
        estilos.deleteById(codigo);
        return ResponseEntity.noContent().build();
    }

}
