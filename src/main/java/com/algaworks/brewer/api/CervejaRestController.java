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

import com.algaworks.brewer.Service.CadastroCervejaService;
import com.algaworks.brewer.Service.Exception.ImpossivelExcluirEntidadeException;
import com.algaworks.brewer.dto.CervejaDTO;
import com.algaworks.brewer.model.Cerveja;
import com.algaworks.brewer.repository.Cervejas;
import com.algaworks.brewer.repository.filter.CervejaFilter;

import jakarta.validation.Valid;

/**
 * REST API Controller for Cervejas (Beers)
 */
@RestController
@RequestMapping("/api/cervejas")
public class CervejaRestController {

    @Autowired
    private Cervejas cervejas;

    @Autowired
    private CadastroCervejaService cadastroCervejaService;

    /**
     * List all cervejas with pagination and filtering
     */
    @GetMapping
    public ResponseEntity<Page<Cerveja>> listar(CervejaFilter filtro, Pageable pageable) {
        Page<Cerveja> page = cervejas.filtrar(filtro, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * Get a cerveja by ID
     */
    @GetMapping("/{codigo}")
    public ResponseEntity<Cerveja> buscar(@PathVariable Long codigo) {
        return cervejas.findById(codigo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Search cervejas by SKU or name
     */
    @GetMapping("/search")
    public ResponseEntity<List<CervejaDTO>> pesquisar(@RequestParam String skuOuNome) {
        List<CervejaDTO> resultado = cervejas.porSkuOuNome(skuOuNome);
        return ResponseEntity.ok(resultado);
    }

    /**
     * Create a new cerveja
     */
    @PostMapping
    public ResponseEntity<Cerveja> criar(@Valid @RequestBody Cerveja cerveja) {
        cadastroCervejaService.salvar(cerveja);
        return ResponseEntity.status(HttpStatus.CREATED).body(cerveja);
    }

    /**
     * Update an existing cerveja
     */
    @PutMapping("/{codigo}")
    public ResponseEntity<Cerveja> atualizar(@PathVariable Long codigo, @Valid @RequestBody Cerveja cerveja) {
        if (!cervejas.existsById(codigo)) {
            return ResponseEntity.notFound().build();
        }
        cerveja.setCodigo(codigo);
        cadastroCervejaService.salvar(cerveja);
        return ResponseEntity.ok(cerveja);
    }

    /**
     * Delete a cerveja
     */
    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> excluir(@PathVariable Long codigo) {
        return cervejas.findById(codigo)
                .map(cerveja -> {
                    try {
                        cadastroCervejaService.excluir(cerveja);
                        return ResponseEntity.noContent().<Void>build();
                    } catch (ImpossivelExcluirEntidadeException e) {
                        return ResponseEntity.badRequest().<Void>build();
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

}
