package com.webrich.bettereads;

import java.nio.file.Path;

import javax.annotation.PostConstruct;

import com.webrich.bettereads.connection.DataStaxAstraProperties;
import com.webrich.bettereads.model.Author;
import com.webrich.bettereads.repository.AuthorRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties (DataStaxAstraProperties.class)
public class BettereadsApplication {

	@Autowired 
	AuthorRepository authorRepository;
	
	public static void main(String[] args) {
		SpringApplication.run(BettereadsApplication.class, args);
	}

	@Bean
	public CqlSessionBuilderCustomizer sessionBuilderCustomizer(DataStaxAstraProperties astraProperties){
		Path bundle = astraProperties.getSecureConnectBundle().toPath();
		return builder->builder.withCloudSecureConnectBundle(bundle);
	}

	@PostConstruct
	public void start(){
		Author author = new Author();
		author.setId("RandomId");
		author.setName("RandomName");
		author.setPersonalName("RandomPersonalName");
		authorRepository.save(author);
	}
}
