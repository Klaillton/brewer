package com.algaworks.brewer.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.algaworks.brewer.model.Cerveja;

public interface Cervejas extends JpaRepository<Cerveja, Long> {
	
}