package com.algaworks.brewer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

@Configuration
public class S3Config {

	@Value("${aws.access.key.id:}")
	private String accessKeyId;

	@Value("${aws.secret.access.key:}")
	private String secretAccessKey;

	@Bean
	public AmazonS3 amazonS3() {
		if (accessKeyId == null || accessKeyId.isEmpty() || secretAccessKey == null || secretAccessKey.isEmpty()) {
			return AmazonS3ClientBuilder.standard()
				.withRegion(Regions.US_EAST_1)
				.build();
		}
		
		AWSCredentials credentials = new BasicAWSCredentials(accessKeyId, secretAccessKey);
		return AmazonS3ClientBuilder.standard()
			.withCredentials(new AWSStaticCredentialsProvider(credentials))
			.withRegion(Regions.US_EAST_1)
			.build();
	}

}
