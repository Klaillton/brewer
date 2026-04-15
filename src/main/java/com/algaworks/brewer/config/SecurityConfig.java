package com.algaworks.brewer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests(authorize -> authorize
				.requestMatchers("/layout/**", "/images/**", "/stylesheets/**", "/static/**").permitAll()
				.requestMatchers("/h2-console/**").permitAll()
				.requestMatchers("/actuator/**").permitAll()
				.requestMatchers("/cidades/novo").hasRole("CADASTRAR_CIDADE")
				.requestMatchers("/usuarios/**").hasRole("CADASTRAR_USUARIO")
				.requestMatchers("/api/estados").permitAll()
				.requestMatchers("/api/cervejas/search").permitAll()
				.requestMatchers("/api/**").authenticated()
				.anyRequest().authenticated()
			)
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
			.csrf(csrf -> csrf
				.ignoringRequestMatchers("/api/**", "/h2-console/**")
			)
			.headers(headers -> headers
				.frameOptions(frame -> frame.sameOrigin())
			);
		
		return http.build();
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
