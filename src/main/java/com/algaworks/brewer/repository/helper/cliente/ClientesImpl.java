package com.algaworks.brewer.repository.helper.cliente;

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

import com.algaworks.brewer.model.Cliente;
import com.algaworks.brewer.repository.filter.ClienteFilter;

public class ClientesImpl implements ClientesQueries {

	@PersistenceContext
	private EntityManager manager;

	@Override
	@Transactional(readOnly = true)
	public Page<Cliente> filtrar(ClienteFilter filtro, Pageable pageable) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Cliente> query = builder.createQuery(Cliente.class);
		Root<Cliente> root = query.from(Cliente.class);
		root.fetch("endereco", JoinType.LEFT).fetch("cidade", JoinType.LEFT).fetch("estado", JoinType.LEFT);

		List<Predicate> predicates = adicionarFiltro(filtro, builder, root);
		query.where(predicates.toArray(new Predicate[0]));

		TypedQuery<Cliente> typedQuery = manager.createQuery(query);
		typedQuery.setFirstResult((int) pageable.getOffset());
		typedQuery.setMaxResults(pageable.getPageSize());

		return new PageImpl<>(typedQuery.getResultList(), pageable, total(filtro));
	}

	@Transactional(readOnly = true)
	@Override
	public Cliente buscarEstadoCidade(Long codigo) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Cliente> query = builder.createQuery(Cliente.class);
		Root<Cliente> root = query.from(Cliente.class);
		root.fetch("endereco", JoinType.LEFT).fetch("cidade", JoinType.LEFT).fetch("estado", JoinType.LEFT);

		query.where(builder.equal(root.get("codigo"), codigo));

		return manager.createQuery(query).getSingleResult();
	}

	private Long total(ClienteFilter filtro) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Long> query = builder.createQuery(Long.class);
		Root<Cliente> root = query.from(Cliente.class);

		List<Predicate> predicates = adicionarFiltro(filtro, builder, root);
		query.where(predicates.toArray(new Predicate[0]));
		query.select(builder.count(root));

		return manager.createQuery(query).getSingleResult();
	}

	private List<Predicate> adicionarFiltro(ClienteFilter filtro, CriteriaBuilder builder, Root<Cliente> root) {
		List<Predicate> predicates = new ArrayList<>();

		if (filtro != null) {
			if (StringUtils.hasText(filtro.getNome())) {
				predicates.add(builder.like(builder.lower(root.get("nome")),
						"%" + filtro.getNome().toLowerCase() + "%"));
			}

			if (StringUtils.hasText(filtro.getCpfOuCnpj())) {
				predicates.add(builder.equal(root.get("cpfOuCnpj"), filtro.getCpfOuCnpjSemFormatacao()));
			}
		}

		return predicates;
	}

}