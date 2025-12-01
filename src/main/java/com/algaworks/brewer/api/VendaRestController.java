package com.algaworks.brewer.api;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.algaworks.brewer.dto.VendaMes;
import com.algaworks.brewer.dto.VendaOrigem;
import com.algaworks.brewer.model.Venda;
import com.algaworks.brewer.repository.Vendas;
import com.algaworks.brewer.repository.filter.VendaFilter;

/**
 * REST API Controller for Vendas (Sales)
 */
@RestController
@RequestMapping("/api/vendas")
public class VendaRestController {

    @Autowired
    private Vendas vendas;

    /**
     * List all vendas with pagination and filtering
     */
    @GetMapping
    public ResponseEntity<Page<Venda>> listar(VendaFilter filtro, Pageable pageable) {
        Page<Venda> page = vendas.filtrar(filtro, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * Get a venda by ID
     */
    @GetMapping("/{codigo}")
    public ResponseEntity<Venda> buscar(@PathVariable Long codigo) {
        Venda venda = vendas.buscarComItens(codigo);
        if (venda != null) {
            return ResponseEntity.ok(venda);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Get total sales value for the year
     */
    @GetMapping("/stats/valor-ano")
    public ResponseEntity<BigDecimal> valorTotalNoAno() {
        return ResponseEntity.ok(vendas.valorTotalNoAno());
    }

    /**
     * Get total sales value for the month
     */
    @GetMapping("/stats/valor-mes")
    public ResponseEntity<BigDecimal> valorTotalNoMes() {
        return ResponseEntity.ok(vendas.valorTotalNoMes());
    }

    /**
     * Get average ticket value for the year
     */
    @GetMapping("/stats/ticket-medio-ano")
    public ResponseEntity<BigDecimal> ticketMedioNoAno() {
        return ResponseEntity.ok(vendas.valorTicketMedioNoAno());
    }

    /**
     * Get sales by month
     */
    @GetMapping("/stats/por-mes")
    public ResponseEntity<List<VendaMes>> totalPorMes() {
        return ResponseEntity.ok(vendas.totalPorMes());
    }

    /**
     * Get sales by origin
     */
    @GetMapping("/stats/por-origem")
    public ResponseEntity<List<VendaOrigem>> totalPorOrigem() {
        return ResponseEntity.ok(vendas.totalPorOrigem());
    }

}
