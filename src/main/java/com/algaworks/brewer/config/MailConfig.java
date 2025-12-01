package com.algaworks.brewer.config;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfig {

	@Value("${mail.username:}")
	private String mailUsername;

	@Value("${mail.password:}")
	private String mailPassword;

	@Value("${mail.host:smtp.sendgrid.net}")
	private String mailHost;

	@Value("${mail.port:587}")
	private int mailPort;

	@Bean
	public JavaMailSender mailSender() {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost(mailHost);
		mailSender.setPort(mailPort);
		mailSender.setUsername(mailUsername);
		mailSender.setPassword(mailPassword);

		Properties props = new Properties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.auth", true);
		props.put("mail.smtp.starttls.enable", true);
		props.put("mail.debug", false);
		props.put("mail.smtp.connectiontimeout", 10000);

		mailSender.setJavaMailProperties(props);

		return mailSender;
	}

}
