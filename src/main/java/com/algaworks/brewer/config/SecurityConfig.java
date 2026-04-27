package com.algaworks.brewer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

	private final Environment environment;

	public SecurityConfig(Environment environment) {
		this.environment = environment;
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		boolean allowH2Console = isH2ConsoleAllowed();

		http
			.authorizeHttpRequests(authorize -> {
				authorize
					.requestMatchers("/layout/**", "/images/**", "/stylesheets/**", "/static/**").permitAll()
					.requestMatchers("/actuator/health", "/actuator/info").permitAll()
					.requestMatchers("/actuator/**").authenticated()
					.requestMatchers("/cidades/novo").hasRole("CADASTRAR_CIDADE")
					.requestMatchers("/usuarios/**").hasRole("CADASTRAR_USUARIO")
					.requestMatchers("/api/estados").permitAll()
					.requestMatchers("/api/cervejas/search").permitAll()
					.requestMatchers("/api/**").authenticated()
					.anyRequest().authenticated();

				if (allowH2Console) {
					authorize.requestMatchers("/h2-console/**").permitAll();
				}
			})
			.formLogin(form -> form
				.loginPage("/login")
				.permitAll()
			)
			.logout(logout -> logout
				.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
			)
			.exceptionHandling(exception -> exception
				.accessDeniedPage("/403")
			)
			.sessionManagement(session -> session
				.invalidSessionUrl("/login")
			)
			.csrf(csrf -> {
				csrf.ignoringRequestMatchers("/api/**");
				if (allowH2Console) {
					csrf.ignoringRequestMatchers("/h2-console/**");
				}
			})
			.headers(headers -> {
				if (allowH2Console) {
					headers.frameOptions(frame -> frame.sameOrigin());
				}
			});
		
		return http.build();
	}

	private boolean isH2ConsoleAllowed() {
		boolean h2ConsoleEnabled = Boolean.parseBoolean(environment.getProperty("spring.h2.console.enabled", "false"));
		boolean localProfile = environment.matchesProfiles("dev", "local", "test");
		return h2ConsoleEnabled && localProfile;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
