package com.algaworks.brewer.repository.helper.usuario;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.algaworks.brewer.model.Grupo;
import com.algaworks.brewer.model.Usuario;
import com.algaworks.brewer.model.UsuarioGrupo;
import com.algaworks.brewer.repository.filter.UsuarioFilter;

public class UsuariosImpl implements UsuariosQueries {

	@PersistenceContext
	private EntityManager manager;

	@Override
	public Optional<Usuario> porEmailEAtivo(String email) {
		return manager
				.createQuery("from Usuario where lower(email) = lower(:email) and ativo = true", Usuario.class)
				.setParameter("email", email).getResultList().stream().findFirst();
	}

	@Override
	public List<String> permissoes(Usuario usuario) {
		return manager.createQuery(
				"select distinct p.nome from Usuario u inner join u.grupos g inner join g.permissoes p where u = :usuario", String.class)
				.setParameter("usuario", usuario)
				.getResultList();
	}

	@Transactional(readOnly = true)
	@Override
	public Page<Usuario> filtrar(UsuarioFilter filtro, Pageable pageable) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Usuario> query = builder.createQuery(Usuario.class);
		Root<Usuario> root = query.from(Usuario.class);

		List<Predicate> predicates = adicionarFiltro(filtro, builder, root, query);
		query.where(predicates.toArray(new Predicate[0]));

		TypedQuery<Usuario> typedQuery = manager.createQuery(query);
		typedQuery.setFirstResult((int) pageable.getOffset());
		typedQuery.setMaxResults(pageable.getPageSize());

		List<Usuario> filtrados = typedQuery.getResultList();
		filtrados.forEach(u -> Hibernate.initialize(u.getGrupos()));
		return new PageImpl<>(filtrados, pageable, total(filtro));
	}

	@Transactional(readOnly = true)
	@Override
	public Usuario buscarComGrupos(Long codigo) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Usuario> query = builder.createQuery(Usuario.class);
		Root<Usuario> root = query.from(Usuario.class);
		root.fetch("grupos", JoinType.LEFT);

		query.where(builder.equal(root.get("codigo"), codigo));
		query.distinct(true);

		return manager.createQuery(query).getSingleResult();
	}

	private Long total(UsuarioFilter filtro) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Long> query = builder.createQuery(Long.class);
		Root<Usuario> root = query.from(Usuario.class);

		List<Predicate> predicates = adicionarFiltro(filtro, builder, root, null);
		query.where(predicates.toArray(new Predicate[0]));
		query.select(builder.count(root));

		return manager.createQuery(query).getSingleResult();
	}

	private List<Predicate> adicionarFiltro(UsuarioFilter filtro, CriteriaBuilder builder, 
			Root<Usuario> root, CriteriaQuery<?> query) {
		List<Predicate> predicates = new ArrayList<>();

		if (filtro != null) {
			if (StringUtils.hasText(filtro.getNome())) {
				predicates.add(builder.like(builder.lower(root.get("nome")),
						"%" + filtro.getNome().toLowerCase() + "%"));
			}

			if (StringUtils.hasText(filtro.getEmail())) {
				predicates.add(builder.like(builder.lower(root.get("email")),
						filtro.getEmail().toLowerCase() + "%"));
			}

			if (filtro.getGrupos() != null && !filtro.getGrupos().isEmpty() && query != null) {
				for (Grupo grupo : filtro.getGrupos()) {
					Subquery<Long> subquery = query.subquery(Long.class);
					Root<UsuarioGrupo> subRoot = subquery.from(UsuarioGrupo.class);
					subquery.select(subRoot.get("id").get("usuario").get("codigo"));
					subquery.where(builder.equal(subRoot.get("id").get("grupo").get("codigo"), grupo.getCodigo()));
					predicates.add(root.get("codigo").in(subquery));
				}
			}
		}

		return predicates;
	}

}