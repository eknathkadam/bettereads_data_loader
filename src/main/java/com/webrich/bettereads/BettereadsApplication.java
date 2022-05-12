package com.webrich.bettereads;

import java.io.File;
import java.nio.file.Path;

import com.webrich.bettereads.connection.DataStaxAstraProperties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties (DataStaxAstraProperties.class)
public class BettereadsApplication {

	public static void main(String[] args) {
		SpringApplication.run(BettereadsApplication.class, args);
	}

	@Bean
	public CqlSessionBuilderCustomizer sessionBuilderCustomizer(DataStaxAstraProperties astraProperties){
		File f = astraProperties.getSecureConnectBundle();
		System.out.println("THE FILE IS:"+f);
		Path bundle = f.toPath();
		System.out.println("THE FILE PATH IS:"+bundle);
		return builder->builder.withCloudSecureConnectBundle(bundle);
	}

}
