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

	private void mergeGroupPermission(Long groupId, Long permissionId) {
		jdbcTemplate.update(
				"MERGE INTO grupo_permissao (codigo_grupo, codigo_permissao) KEY(codigo_grupo, codigo_permissao) VALUES (?, ?)",
				groupId, permissionId);
	}
}