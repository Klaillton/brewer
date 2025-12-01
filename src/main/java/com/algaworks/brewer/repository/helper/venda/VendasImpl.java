package com.algaworks.brewer.repository.helper.venda;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.Year;
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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.algaworks.brewer.dto.VendaMes;
import com.algaworks.brewer.dto.VendaOrigem;
import com.algaworks.brewer.model.StatusVenda;
import com.algaworks.brewer.model.TipoPessoa;
import com.algaworks.brewer.model.Venda;
import com.algaworks.brewer.repository.filter.VendaFilter;

public class VendasImpl implements VendasQueries {

	@PersistenceContext
	private EntityManager manager;

	@Transactional(readOnly = true)
	@Override
	public Page<Venda> filtrar(VendaFilter filtro, Pageable pageable) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Venda> query = builder.createQuery(Venda.class);
		Root<Venda> root = query.from(Venda.class);
		root.fetch("cliente", JoinType.LEFT);

		List<Predicate> predicates = adicionarFiltro(filtro, builder, root);
		query.where(predicates.toArray(new Predicate[0]));

		TypedQuery<Venda> typedQuery = manager.createQuery(query);
		typedQuery.setFirstResult((int) pageable.getOffset());
		typedQuery.setMaxResults(pageable.getPageSize());

		return new PageImpl<>(typedQuery.getResultList(), pageable, total(filtro));
	}

	@Transactional(readOnly = true)
	@Override
	public Venda buscarComItens(Long codigo) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Venda> query = builder.createQuery(Venda.class);
		Root<Venda> root = query.from(Venda.class);
		root.fetch("itens", JoinType.LEFT);

		query.where(builder.equal(root.get("codigo"), codigo));
		query.distinct(true);

		return manager.createQuery(query).getSingleResult();
	}

	@Override
	public BigDecimal valorTotalNoAno() {
		Optional<BigDecimal> optional = Optional.ofNullable(manager
				.createQuery("select sum(valorTotal) from Venda where year(dataCriacao) = :ano and status = :status",
						BigDecimal.class)
				.setParameter("ano", Year.now().getValue()).setParameter("status", StatusVenda.EMITIDA)
				.getSingleResult());
		return optional.orElse(BigDecimal.ZERO);
	}

	@Override
	public BigDecimal valorTotalNoMes() {
		Optional<BigDecimal> optional = Optional.ofNullable(manager.createQuery(
				"select sum(valorTotal) from Venda where year(dataCriacao) = :ano and month(dataCriacao) = :mes and status = :status",
				BigDecimal.class).setParameter("ano", Year.now().getValue())
				.setParameter("mes", MonthDay.now().getMonthValue()).setParameter("status", StatusVenda.EMITIDA)
				.getSingleResult());
		return optional.orElse(BigDecimal.ZERO);
	}

	@Override
	public BigDecimal valorTicketMedioNoAno() {
		Optional<BigDecimal> optional = Optional.ofNullable(manager.createQuery(
				"select sum(valorTotal)/count(*) from Venda where year(dataCriacao) = :ano and status = :status",
				BigDecimal.class).setParameter("ano", Year.now().getValue()).setParameter("status", StatusVenda.EMITIDA)
				.getSingleResult());
		return optional.orElse(BigDecimal.ZERO);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<VendaMes> totalPorMes() {
		List<VendaMes> vendasMes = manager.createNamedQuery("Vendas.totalPorMes").getResultList();

		LocalDate hoje = LocalDate.now();
		for (int i = 1; i <= 6; i++) {
			String mesIdeal = String.format("%d/%02d", hoje.getYear(), hoje.getMonthValue());

			boolean possuiMes = vendasMes.stream().filter(v -> v.getMes().equals(mesIdeal)).findAny().isPresent();
			if (!possuiMes) {
				vendasMes.add(i - 1, new VendaMes(mesIdeal, 0));
			}

			hoje = hoje.minusMonths(1);
		}

		return vendasMes;
	}

	@Override
	public List<VendaOrigem> totalPorOrigem() {
		List<VendaOrigem> vendasNacionalidade = manager.createNamedQuery("Vendas.porOrigem", VendaOrigem.class)
				.getResultList();

		LocalDate now = LocalDate.now();
		for (int i = 1; i <= 6; i++) {
			String mesIdeal = String.format("%d/%02d", now.getYear(), now.getMonth().getValue());

			boolean possuiMes = vendasNacionalidade.stream().filter(v -> v.getMes().equals(mesIdeal)).findAny()
					.isPresent();
			if (!possuiMes) {
				vendasNacionalidade.add(i - 1, new VendaOrigem(mesIdeal, 0, 0));
			}

			now = now.minusMonths(1);
		}

		return vendasNacionalidade;
	}

	private Long total(VendaFilter filtro) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Long> query = builder.createQuery(Long.class);
		Root<Venda> root = query.from(Venda.class);
		root.join("cliente", JoinType.LEFT);

		List<Predicate> predicates = adicionarFiltro(filtro, builder, root);
		query.where(predicates.toArray(new Predicate[0]));
		query.select(builder.count(root));

		return manager.createQuery(query).getSingleResult();
	}

	private List<Predicate> adicionarFiltro(VendaFilter filtro, CriteriaBuilder builder, Root<Venda> root) {
		List<Predicate> predicates = new ArrayList<>();

		if (filtro != null) {
			if (filtro.getCodigo() != null) {
				predicates.add(builder.equal(root.get("codigo"), filtro.getCodigo()));
			}

			if (filtro.getStatus() != null) {
				predicates.add(builder.equal(root.get("status"), filtro.getStatus()));
			}

			if (filtro.getDesde() != null) {
				LocalDateTime desde = LocalDateTime.of(filtro.getDesde(), LocalTime.of(0, 0));
				predicates.add(builder.greaterThanOrEqualTo(root.get("dataCriacao"), desde));
			}

			if (filtro.getAte() != null) {
				LocalDateTime ate = LocalDateTime.of(filtro.getAte(), LocalTime.of(23, 59));
				predicates.add(builder.lessThanOrEqualTo(root.get("dataCriacao"), ate));
			}

			if (filtro.getValorMinimo() != null) {
				predicates.add(builder.greaterThanOrEqualTo(root.get("valorTotal"), filtro.getValorMinimo()));
			}

			if (filtro.getValorMaximo() != null) {
				predicates.add(builder.lessThanOrEqualTo(root.get("valorTotal"), filtro.getValorMaximo()));
			}

			if (StringUtils.hasText(filtro.getNomeCliente())) {
				predicates.add(builder.like(builder.lower(root.get("cliente").get("nome")),
						"%" + filtro.getNomeCliente().toLowerCase() + "%"));
			}

			if (StringUtils.hasText(filtro.getCpfOuCnpjCliente())) {
				predicates.add(builder.equal(root.get("cliente").get("cpfOuCnpj"),
						TipoPessoa.removerFormatacao(filtro.getCpfOuCnpjCliente())));
			}
		}

		return predicates;
	}

}
