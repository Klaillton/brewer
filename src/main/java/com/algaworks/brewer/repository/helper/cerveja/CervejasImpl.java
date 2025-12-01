package com.algaworks.brewer.repository.helper.cerveja;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.algaworks.brewer.dto.CervejaDTO;
import com.algaworks.brewer.dto.ValorItensEstoque;
import com.algaworks.brewer.model.Cerveja;
import com.algaworks.brewer.repository.filter.CervejaFilter;
import com.algaworks.brewer.storage.FotoStorage;

public class CervejasImpl implements CervejasQueries {

	@PersistenceContext
	private EntityManager manager;

	@Autowired
	private FotoStorage fotoStorage;

	@Override
	@Transactional(readOnly = true)
	public Page<Cerveja> filtrar(CervejaFilter filtro, Pageable pageable) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Cerveja> query = builder.createQuery(Cerveja.class);
		Root<Cerveja> root = query.from(Cerveja.class);

		List<Predicate> predicates = adicionarFiltro(filtro, builder, root);
		query.where(predicates.toArray(new Predicate[0]));

		TypedQuery<Cerveja> typedQuery = manager.createQuery(query);
		typedQuery.setFirstResult((int) pageable.getOffset());
		typedQuery.setMaxResults(pageable.getPageSize());

		return new PageImpl<>(typedQuery.getResultList(), pageable, total(filtro));
	}

	@Override
	public List<CervejaDTO> porSkuOuNome(String skuOuNome) {
		String jpql = "select new com.algaworks.brewer.dto.CervejaDTO(codigo, sku, nome, origem, valor, foto) "
				+ "from Cerveja where lower(sku) like lower(:skuOuNome) or lower(nome) like lower(:skuOuNome)";
		List<CervejaDTO> cervejasFiltradas = manager.createQuery(jpql, CervejaDTO.class)
				.setParameter("skuOuNome", skuOuNome + "%")
				.getResultList();
		cervejasFiltradas.forEach(c -> c.setUrlThumbnailFoto(fotoStorage.getUrl(FotoStorage.THUMBNAIL_PREFIX + c.getFoto())));
		return cervejasFiltradas;
	}

	@Override
	public ValorItensEstoque valorItensEstoque() {
		String query = "select new com.algaworks.brewer.dto.ValorItensEstoque(sum(valor * quantidadeEstoque), sum(quantidadeEstoque)) from Cerveja";
		return manager.createQuery(query, ValorItensEstoque.class).getSingleResult();
	}

	private Long total(CervejaFilter filtro) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Long> query = builder.createQuery(Long.class);
		Root<Cerveja> root = query.from(Cerveja.class);

		List<Predicate> predicates = adicionarFiltro(filtro, builder, root);
		query.where(predicates.toArray(new Predicate[0]));
		query.select(builder.count(root));

		return manager.createQuery(query).getSingleResult();
	}

	private List<Predicate> adicionarFiltro(CervejaFilter filtro, CriteriaBuilder builder, Root<Cerveja> root) {
		List<Predicate> predicates = new ArrayList<>();

		if (filtro != null) {
			if (StringUtils.hasText(filtro.getSku())) {
				predicates.add(builder.equal(root.get("sku"), filtro.getSku()));
			}

			if (StringUtils.hasText(filtro.getNome())) {
				predicates.add(builder.like(builder.lower(root.get("nome")), 
						"%" + filtro.getNome().toLowerCase() + "%"));
			}

			if (isEstiloPresente(filtro)) {
				predicates.add(builder.equal(root.get("estilo"), filtro.getEstilo()));
			}

			if (filtro.getSabor() != null) {
				predicates.add(builder.equal(root.get("sabor"), filtro.getSabor()));
			}

			if (filtro.getOrigem() != null) {
				predicates.add(builder.equal(root.get("origem"), filtro.getOrigem()));
			}

			if (filtro.getValorDe() != null) {
				predicates.add(builder.greaterThanOrEqualTo(root.get("valor"), filtro.getValorDe()));
			}

			if (filtro.getValorAte() != null) {
				predicates.add(builder.lessThanOrEqualTo(root.get("valor"), filtro.getValorAte()));
			}
		}

		return predicates;
	}

	private boolean isEstiloPresente(CervejaFilter filtro) {
		return filtro.getEstilo() != null && filtro.getEstilo().getCodigo() != null;
	}

}