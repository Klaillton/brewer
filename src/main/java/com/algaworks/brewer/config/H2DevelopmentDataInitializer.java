package com.algaworks.brewer.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "spring.datasource.driverClassName", havingValue = "org.h2.Driver")
public class H2DevelopmentDataInitializer implements ApplicationRunner {

	private static final String ADMIN_EMAIL = "admin@brewer.com";
	private static final String ADMIN_PASSWORD_HASH = "$2a$10$x3dW.vGNa.OsxIBZ7qi36uScizK1I1UspCXjasBlnZ31k5yiw.KCa";

	private final JdbcTemplate jdbcTemplate;

	public H2DevelopmentDataInitializer(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		seedGroups();
		seedPermissions();
		seedGroupPermissions();
		seedEstados();
		seedCidades();
		seedAdminUser();
		seedAdminGroupMembership();
	}

	private void seedGroups() {
		jdbcTemplate.update("MERGE INTO grupo (codigo, nome) KEY(codigo) VALUES (?, ?)", 1L, "Administrador");
		jdbcTemplate.update("MERGE INTO grupo (codigo, nome) KEY(codigo) VALUES (?, ?)", 2L, "Vendedor");
	}

	private void seedPermissions() {
		jdbcTemplate.update("MERGE INTO permissao (codigo, nome) KEY(codigo) VALUES (?, ?)", 1L,
				"ROLE_CADASTRAR_CIDADE");
		jdbcTemplate.update("MERGE INTO permissao (codigo, nome) KEY(codigo) VALUES (?, ?)", 2L,
				"ROLE_CADASTRAR_USUARIO");
		jdbcTemplate.update("MERGE INTO permissao (codigo, nome) KEY(codigo) VALUES (?, ?)", 3L,
				"ROLE_CANCELAR_VENDA");
	}

	private void seedGroupPermissions() {
		mergeGroupPermission(1L, 1L);
		mergeGroupPermission(1L, 2L);
		mergeGroupPermission(1L, 3L);
	}

	private void seedAdminUser() {
		jdbcTemplate.update(
				"INSERT INTO usuario (nome, email, senha, ativo, data_nascimento) "
						+ "SELECT ?, ?, ?, ?, DATE '1990-01-01' "
						+ "WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE email = ?)",
				"Admin", ADMIN_EMAIL, ADMIN_PASSWORD_HASH, true, ADMIN_EMAIL);
	}

	private void seedAdminGroupMembership() {
		jdbcTemplate.update(
				"INSERT INTO usuario_grupo (codigo_usuario, codigo_grupo) "
						+ "SELECT u.codigo, ? FROM usuario u "
						+ "WHERE u.email = ? "
						+ "AND NOT EXISTS (SELECT 1 FROM usuario_grupo ug WHERE ug.codigo_usuario = u.codigo AND ug.codigo_grupo = ?)",
				1L, ADMIN_EMAIL, 1L);
	}

	private void seedEstados() {
		mergeEstado(1L, "Acre", "AC");
		mergeEstado(2L, "Bahia", "BA");
		mergeEstado(3L, "Goiás", "GO");
		mergeEstado(4L, "Minas Gerais", "MG");
		mergeEstado(5L, "Santa Catarina", "SC");
		mergeEstado(6L, "São Paulo", "SP");
		mergeEstado(7L, "Alagoas", "AL");
		mergeEstado(8L, "Amapá", "AP");
		mergeEstado(9L, "Amazonas", "AM");
		mergeEstado(10L, "Ceará", "CE");
		mergeEstado(11L, "Distrito Federal", "DF");
		mergeEstado(12L, "Espírito Santo", "ES");
		mergeEstado(13L, "Maranhão", "MA");
		mergeEstado(14L, "Mato Grosso", "MT");
		mergeEstado(15L, "Mato Grosso do Sul", "MS");
		mergeEstado(16L, "Pará", "PA");
		mergeEstado(17L, "Paraíba", "PB");
		mergeEstado(18L, "Paraná", "PR");
		mergeEstado(19L, "Pernambuco", "PE");
		mergeEstado(20L, "Piauí", "PI");
		mergeEstado(21L, "Rio de Janeiro", "RJ");
		mergeEstado(22L, "Rio Grande do Norte", "RN");
		mergeEstado(23L, "Rio Grande do Sul", "RS");
		mergeEstado(24L, "Rondônia", "RO");
		mergeEstado(25L, "Roraima", "RR");
		mergeEstado(26L, "Sergipe", "SE");
		mergeEstado(27L, "Tocantins", "TO");
	}

	private void seedCidades() {
		mergeCidade("Rio Branco", 1L);
		mergeCidade("Cruzeiro do Sul", 1L);
		mergeCidade("Salvador", 2L);
		mergeCidade("Feira de Santana", 2L);
		mergeCidade("Porto Seguro", 2L);
		mergeCidade("Goiânia", 3L);
		mergeCidade("Anápolis", 3L);
		mergeCidade("Itumbiara", 3L);
		mergeCidade("Belo Horizonte", 4L);
		mergeCidade("Uberlândia", 4L);
		mergeCidade("Montes Claros", 4L);
		mergeCidade("Florianópolis", 5L);
		mergeCidade("Criciúma", 5L);
		mergeCidade("Balneário Camboriú", 5L);
		mergeCidade("Lages", 5L);
		mergeCidade("São Paulo", 6L);
		mergeCidade("Campinas", 6L);
		mergeCidade("Santos", 6L);
		mergeCidade("Ribeirão Preto", 6L);
		mergeCidade("Maceió", 7L);
		mergeCidade("Arapiraca", 7L);
		mergeCidade("Macapá", 8L);
		mergeCidade("Santana", 8L);
		mergeCidade("Manaus", 9L);
		mergeCidade("Parintins", 9L);
		mergeCidade("Fortaleza", 10L);
		mergeCidade("Juazeiro do Norte", 10L);
		mergeCidade("Brasília", 11L);
		mergeCidade("Vitória", 12L);
		mergeCidade("Vila Velha", 12L);
		mergeCidade("São Luís", 13L);
		mergeCidade("Imperatriz", 13L);
		mergeCidade("Cuiabá", 14L);
		mergeCidade("Rondonópolis", 14L);
		mergeCidade("Campo Grande", 15L);
		mergeCidade("Dourados", 15L);
		mergeCidade("Belém", 16L);
		mergeCidade("Santarém", 16L);
		mergeCidade("João Pessoa", 17L);
		mergeCidade("Campina Grande", 17L);
		mergeCidade("Curitiba", 18L);
		mergeCidade("Londrina", 18L);
		mergeCidade("Maringá", 18L);
		mergeCidade("Recife", 19L);
		mergeCidade("Caruaru", 19L);
		mergeCidade("Teresina", 20L);
		mergeCidade("Parnaíba", 20L);
		mergeCidade("Rio de Janeiro", 21L);
		mergeCidade("Niterói", 21L);
		mergeCidade("Natal", 22L);
		mergeCidade("Mossoró", 22L);
		mergeCidade("Porto Alegre", 23L);
		mergeCidade("Caxias do Sul", 23L);
		mergeCidade("Porto Velho", 24L);
		mergeCidade("Ji-Paraná", 24L);
		mergeCidade("Boa Vista", 25L);
		mergeCidade("Rorainópolis", 25L);
		mergeCidade("Aracaju", 26L);
		mergeCidade("Itabaiana", 26L);
		mergeCidade("Palmas", 27L);
		mergeCidade("Araguaína", 27L);
	}

	private void mergeEstado(Long codigo, String nome, String sigla) {
		jdbcTemplate.update("MERGE INTO estado (codigo, nome, sigla) KEY(codigo) VALUES (?, ?, ?)", codigo, nome,
				sigla);
	}

	private void mergeCidade(String nome, Long codigoEstado) {
		jdbcTemplate.update(
				"INSERT INTO cidade (nome, codigo_estado) "
						+ "SELECT ?, ? WHERE NOT EXISTS (SELECT 1 FROM cidade WHERE lower(nome) = lower(?) AND codigo_estado = ?)",
				nome, codigoEstado, nome, codigoEstado);
	}

	private void mergeGroupPermission(Long groupId, Long permissionId) {
		jdbcTemplate.update(
				"MERGE INTO grupo_permissao (codigo_grupo, codigo_permissao) KEY(codigo_grupo, codigo_permissao) VALUES (?, ?)",
				groupId, permissionId);
	}
}