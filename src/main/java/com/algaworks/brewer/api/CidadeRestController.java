package com.algaworks.brewer.api;

import java.util.List;

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

import com.algaworks.brewer.Service.CadastroCidadeService;
import com.algaworks.brewer.model.Cidade;
import com.algaworks.brewer.repository.Cidades;
import com.algaworks.brewer.repository.filter.CidadeFilter;

import jakarta.validation.Valid;

/**
 * REST API Controller for Cidades (Cities)
 */
@RestController
@RequestMapping("/api/cidades")
public class CidadeRestController {

    @Autowired
    private Cidades cidades;

    @Autowired
    private CadastroCidadeService cadastroCidadeService;

    /**
     * List all cidades with pagination and filtering
     */
    @GetMapping
    public ResponseEntity<Page<Cidade>> listar(CidadeFilter filtro, Pageable pageable) {
        Page<Cidade> page = cidades.filtrar(filtro, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * Get cidades by estado
     */
    @GetMapping("/estado/{codigoEstado}")
    public ResponseEntity<List<Cidade>> buscarPorEstado(@PathVariable Long codigoEstado) {
        List<Cidade> cidadesDoEstado = cidades.findByEstadoCodigo(codigoEstado);
        return ResponseEntity.ok(cidadesDoEstado);
    }

    /**
     * Get a cidade by ID
     */
    @GetMapping("/{codigo}")
    public ResponseEntity<Cidade> buscar(@PathVariable Long codigo) {
        Cidade cidade = cidades.buscarComEstado(codigo);
        if (cidade != null) {
            return ResponseEntity.ok(cidade);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Create a new cidade
     */
    @PostMapping
    public ResponseEntity<Cidade> criar(@Valid @RequestBody Cidade cidade) {
        cadastroCidadeService.salvar(cidade);
        return ResponseEntity.status(HttpStatus.CREATED).body(cidade);
    }

    /**
     * Update an existing cidade
     */
    @PutMapping("/{codigo}")
    public ResponseEntity<Cidade> atualizar(@PathVariable Long codigo, @Valid @RequestBody Cidade cidade) {
        if (!cidades.existsById(codigo)) {
            return ResponseEntity.notFound().build();
        }
        cidade.setCodigo(codigo);
        cadastroCidadeService.salvar(cidade);
        return ResponseEntity.ok(cidade);
    }

}
