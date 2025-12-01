package com.algaworks.brewer.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.algaworks.brewer.model.Estado;
import com.algaworks.brewer.repository.Estados;

/**
 * REST API Controller for Estados (States)
 */
@RestController
@RequestMapping("/api/estados")
public class EstadoRestController {

    @Autowired
    private Estados estados;

    /**
     * Get all estados
     */
    @GetMapping
    public ResponseEntity<List<Estado>> listarTodos() {
        return ResponseEntity.ok(estados.findAll());
    }

}
