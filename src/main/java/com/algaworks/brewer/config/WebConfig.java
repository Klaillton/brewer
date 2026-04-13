package com.algaworks.brewer.config;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

import javax.cache.Caching;

import org.springframework.beans.BeansException;
import org.springframework.cache.CacheManager;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.format.FormatterRegistry;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ITemplateResolver;

import com.algaworks.brewer.config.format.BigDecimalFormatter;
import com.algaworks.brewer.controller.converter.CidadeConverter;
import com.algaworks.brewer.controller.converter.EstadoConverter;
import com.algaworks.brewer.controller.converter.EstiloConverter;
import com.algaworks.brewer.controller.converter.GrupoConverter;
import com.algaworks.brewer.thymeleaf.BrewerDialect;
import com.github.mxab.thymeleaf.extras.dataattribute.dialect.DataAttributeDialect;

import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;

@Configuration
public class WebConfig implements ApplicationContextAware, WebMvcConfigurer {

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Bean
	public ViewResolver viewResolver() {
		ThymeleafViewResolver resolver = new ThymeleafViewResolver();
		resolver.setTemplateEngine(templateEngine());
		resolver.setCharacterEncoding("UTF-8");
		resolver.setOrder(1);
		return resolver;
	}

	@Bean
	public SpringTemplateEngine templateEngine() {
		SpringTemplateEngine engine = new SpringTemplateEngine();
		engine.setEnableSpringELCompiler(true);
		engine.setTemplateResolver(templateResolver());

		engine.addDialect(new LayoutDialect());
		engine.addDialect(new BrewerDialect());
		engine.addDialect(new DataAttributeDialect());
		engine.addDialect(new SpringSecurityDialect());
		return engine;
	}

	private ITemplateResolver templateResolver() {
		SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
		resolver.setApplicationContext(applicationContext);
		resolver.setPrefix("classpath:/templates/");
		resolver.setSuffix(".html");
		resolver.setTemplateMode(TemplateMode.HTML);
		resolver.setCharacterEncoding("UTF-8");
		return resolver;
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/**")
			.allowedOriginPatterns("http://localhost:*", "https://localhost:*")
			.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
			.allowedHeaders("*")
			.allowCredentials(true);
	}

	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addConverter(new EstiloConverter());
		registry.addConverter(new CidadeConverter());
		registry.addConverter(new EstadoConverter());
		registry.addConverter(new GrupoConverter());

		BigDecimalFormatter bigDecimalFormatter = new BigDecimalFormatter("#,##0.00");
		registry.addFormatterForFieldType(BigDecimal.class, bigDecimalFormatter);

		BigDecimalFormatter integerFormatter = new BigDecimalFormatter("#,##0");
		registry.addFormatterForFieldType(Integer.class, integerFormatter);

		// Api de datas do Java
		DateTimeFormatterRegistrar dateTimeFormatter = new DateTimeFormatterRegistrar();
		dateTimeFormatter.setDateFormatter(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
		dateTimeFormatter.setTimeFormatter(DateTimeFormatter.ofPattern("HH:mm"));
		dateTimeFormatter.registerFormatters(registry);
	}

	@Bean
	public CacheManager cacheManager() throws Exception {
		return new JCacheCacheManager(Caching.getCachingProvider().getCacheManager(
				getClass().getResource("/cache/ehcache.xml").toURI(),
				getClass().getClassLoader()));
	}

	@Bean
	public MessageSource messageSource() {
		ReloadableResourceBundleMessageSource bundle = new ReloadableResourceBundleMessageSource();
		bundle.setBasename("classpath:/messages");
		bundle.setDefaultEncoding("UTF-8");
		return bundle;
	}

	@Bean
	public LocalValidatorFactoryBean validator() {
		LocalValidatorFactoryBean validatorFactoryBean = new LocalValidatorFactoryBean();
		validatorFactoryBean.setValidationMessageSource(messageSource());
		return validatorFactoryBean;
	}

	@Override
	public Validator getValidator() {
		return validator();
	}

}
