package com.algaworks.brewer.repository.helper.estilos;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.algaworks.brewer.model.Estilo;
import com.algaworks.brewer.repository.filter.EstiloFilter;

public class EstilosImpl implements EstilosQueries {

	@PersistenceContext
	private EntityManager manager;

	@Override
	@Transactional(readOnly = true)
	public Page<Estilo> filtrar(EstiloFilter filtro, Pageable pageable) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Estilo> query = builder.createQuery(Estilo.class);
		Root<Estilo> root = query.from(Estilo.class);

		List<Predicate> predicates = adicionarFiltro(filtro, builder, root);
		query.where(predicates.toArray(new Predicate[0]));

		TypedQuery<Estilo> typedQuery = manager.createQuery(query);
		typedQuery.setFirstResult((int) pageable.getOffset());
		typedQuery.setMaxResults(pageable.getPageSize());

		return new PageImpl<>(typedQuery.getResultList(), pageable, total(filtro));
	}

	private Long total(EstiloFilter filtro) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Long> query = builder.createQuery(Long.class);
		Root<Estilo> root = query.from(Estilo.class);

		List<Predicate> predicates = adicionarFiltro(filtro, builder, root);
		query.where(predicates.toArray(new Predicate[0]));
		query.select(builder.count(root));

		return manager.createQuery(query).getSingleResult();
	}

	private List<Predicate> adicionarFiltro(EstiloFilter filtro, CriteriaBuilder builder, Root<Estilo> root) {
		List<Predicate> predicates = new ArrayList<>();

		if (filtro != null) {
			if (StringUtils.hasText(filtro.getNome())) {
				predicates.add(builder.like(builder.lower(root.get("nome")),
						"%" + filtro.getNome().toLowerCase() + "%"));
			}
		}

		return predicates;
	}

}
