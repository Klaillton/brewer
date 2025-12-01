package com.algaworks.brewer.repository.helper.cidade;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.algaworks.brewer.model.Cidade;
import com.algaworks.brewer.repository.filter.CidadeFilter;

public class CidadesImpl implements CidadesQueries {

	@PersistenceContext
	private EntityManager manager;

	@Override
	@Transactional(readOnly = true)
	public Page<Cidade> filtrar(CidadeFilter filtro, Pageable pageable) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Cidade> query = builder.createQuery(Cidade.class);
		Root<Cidade> root = query.from(Cidade.class);
		root.fetch("estado", JoinType.LEFT);

		List<Predicate> predicates = adicionarFiltro(filtro, builder, root);
		query.where(predicates.toArray(new Predicate[0]));

		TypedQuery<Cidade> typedQuery = manager.createQuery(query);
		typedQuery.setFirstResult((int) pageable.getOffset());
		typedQuery.setMaxResults(pageable.getPageSize());

		return new PageImpl<>(typedQuery.getResultList(), pageable, total(filtro));
	}

	@Override
	@Transactional(readOnly = true)
	public Cidade buscarComEstado(Long codigo) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Cidade> query = builder.createQuery(Cidade.class);
		Root<Cidade> root = query.from(Cidade.class);
		root.fetch("estado", JoinType.LEFT);

		query.where(builder.equal(root.get("codigo"), codigo));

		return manager.createQuery(query).getSingleResult();
	}

	private Long total(CidadeFilter filtro) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Long> query = builder.createQuery(Long.class);
		Root<Cidade> root = query.from(Cidade.class);

		List<Predicate> predicates = adicionarFiltro(filtro, builder, root);
		query.where(predicates.toArray(new Predicate[0]));
		query.select(builder.count(root));

		return manager.createQuery(query).getSingleResult();
	}

	private List<Predicate> adicionarFiltro(CidadeFilter filtro, CriteriaBuilder builder, Root<Cidade> root) {
		List<Predicate> predicates = new ArrayList<>();

		if (filtro != null) {
			if (StringUtils.hasText(filtro.getNome())) {
				predicates.add(builder.like(builder.lower(root.get("nome")),
						"%" + filtro.getNome().toLowerCase() + "%"));
			}

			if (filtro.getEstado() != null) {
				predicates.add(builder.equal(root.get("estado"), filtro.getEstado()));
			}
		}

		return predicates;
	}

}