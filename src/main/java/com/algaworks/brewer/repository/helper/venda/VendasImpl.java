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
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.algaworks.brewer.dto.VendaMes;
import com.algaworks.brewer.dto.VendaOrigem;
import com.algaworks.brewer.model.Origem;
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
		adicionarOrdenacao(pageable, builder, query, root);

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

	@Override
	public List<VendaMes> totalPorMes() {
		LocalDate hoje = LocalDate.now();
		LocalDateTime inicio = LocalDateTime.of(hoje.minusMonths(5).withDayOfMonth(1), LocalTime.MIN);

		List<Object[]> resultados = manager.createQuery(
				"select year(v.dataCriacao), month(v.dataCriacao), count(v) "
						+ "from Venda v "
						+ "where v.dataCriacao >= :inicio and v.status = :status "
						+ "group by year(v.dataCriacao), month(v.dataCriacao) "
						+ "order by year(v.dataCriacao) desc, month(v.dataCriacao) desc",
				Object[].class)
				.setParameter("inicio", inicio)
				.setParameter("status", StatusVenda.EMITIDA)
				.getResultList();

		List<VendaMes> vendasMes = new ArrayList<>();
		for (Object[] resultado : resultados) {
			int ano = ((Number) resultado[0]).intValue();
			int mes = ((Number) resultado[1]).intValue();
			int total = ((Number) resultado[2]).intValue();
			vendasMes.add(new VendaMes(String.format("%d/%02d", ano, mes), total));
		}

		for (int i = 1; i <= 6; i++) {
			String mesIdeal = String.format("%d/%02d", hoje.getYear(), hoje.getMonthValue());

			boolean possuiMes = vendasMes.stream().anyMatch(v -> v.getMes().equals(mesIdeal));
			if (!possuiMes) {
				vendasMes.add(i - 1, new VendaMes(mesIdeal, 0));
			}

			hoje = hoje.minusMonths(1);
		}

		return vendasMes;
	}

	@Override
	public List<VendaOrigem> totalPorOrigem() {
		LocalDate now = LocalDate.now();
		LocalDateTime inicio = LocalDateTime.of(now.minusMonths(5).withDayOfMonth(1), LocalTime.MIN);

		List<Object[]> resultados = manager.createQuery(
				"select year(v.dataCriacao), month(v.dataCriacao), "
						+ "coalesce(sum(case when c.origem = :nacional then i.quantidade else 0 end), 0), "
						+ "coalesce(sum(case when c.origem = :internacional then i.quantidade else 0 end), 0) "
						+ "from ItemVenda i join i.venda v join i.cerveja c "
						+ "where v.dataCriacao >= :inicio and v.status = :status "
						+ "group by year(v.dataCriacao), month(v.dataCriacao) "
						+ "order by year(v.dataCriacao) desc, month(v.dataCriacao) desc",
				Object[].class)
				.setParameter("inicio", inicio)
				.setParameter("status", StatusVenda.EMITIDA)
				.setParameter("nacional", Origem.NACIONAL)
				.setParameter("internacional", Origem.INTERNACIONAL)
				.getResultList();

		List<VendaOrigem> vendasNacionalidade = new ArrayList<>();
		for (Object[] resultado : resultados) {
			int ano = ((Number) resultado[0]).intValue();
			int mes = ((Number) resultado[1]).intValue();
			int totalNacional = ((Number) resultado[2]).intValue();
			int totalInternacional = ((Number) resultado[3]).intValue();
			vendasNacionalidade.add(
					new VendaOrigem(String.format("%d/%02d", ano, mes), totalNacional, totalInternacional));
		}

		for (int i = 1; i <= 6; i++) {
			String mesIdeal = String.format("%d/%02d", now.getYear(), now.getMonth().getValue());

			boolean possuiMes = vendasNacionalidade.stream().anyMatch(v -> v.getMes().equals(mesIdeal));
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

	private void adicionarOrdenacao(Pageable pageable, CriteriaBuilder builder,
			CriteriaQuery<Venda> query, Root<Venda> root) {
		if (pageable == null || pageable.getSort().isUnsorted()) {
			return;
		}

		List<jakarta.persistence.criteria.Order> orders = new ArrayList<>();
		for (Sort.Order sortOrder : pageable.getSort()) {
			String property = normalizarPropriedadeOrdenacao(sortOrder.getProperty());
			Path<?> path = resolverPath(root, property);
			orders.add(sortOrder.isAscending() ? builder.asc(path) : builder.desc(path));
		}

		if (!orders.isEmpty()) {
			query.orderBy(orders);
		}
	}

	private String normalizarPropriedadeOrdenacao(String propriedade) {
		if ("c.nome".equals(propriedade)) {
			return "cliente.nome";
		}

		if ("usuario".equals(propriedade)) {
			return "usuario.nome";
		}

		return propriedade;
	}

	private Path<?> resolverPath(Root<Venda> root, String propriedade) {
		Path<?> path = root;
		for (String trecho : propriedade.split("\\.")) {
			path = path.get(trecho);
		}
		return path;
	}

}
