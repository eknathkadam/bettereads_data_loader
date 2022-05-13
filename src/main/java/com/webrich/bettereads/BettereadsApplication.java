package com.webrich.bettereads;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import com.webrich.bettereads.connection.DataStaxAstraProperties;
import com.webrich.bettereads.model.Author;
import com.webrich.bettereads.repository.AuthorRepository;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties(DataStaxAstraProperties.class)
public class BettereadsApplication {

	@Autowired
	AuthorRepository authorRepository;

	@Value("${datadump.location.author}")
	private String authorDumpLocation;

	@Value("${datadump.location.works}")
	private String worksDumpLocation;

	public static void main(String[] args) {
		SpringApplication.run(BettereadsApplication.class, args);
	}

	@Bean
	public CqlSessionBuilderCustomizer sessionBuilderCustomizer(DataStaxAstraProperties astraProperties) {
		Path bundle = astraProperties.getSecureConnectBundle().toPath();
		return builder -> builder.withCloudSecureConnectBundle(bundle);
	}

	private void initAuthors() {
		Path path = Paths.get(authorDumpLocation);

		try (Stream<String> lines = Files.lines(path)) {
			lines.limit(10).forEach(line -> {
				String jsonString = line.substring(line.indexOf("{"));
				try {
					JSONObject json = new JSONObject(jsonString);
					Author author = new Author();
					author.setId(json.optString("key").replace("/authors/", ""));
					author.setName(json.optString("name"));
					author.setPersonalName(json.optString("personal_name"));
					authorRepository.save(author);
				} catch (JSONException e) {
					e.printStackTrace();
				}

			});
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void initWorks() {

	}

	@PostConstruct
	public void start() {
		// Author author = new Author();
		// author.setId("RandomId");
		// author.setName("RandomName");
		// author.setPersonalName("RandomPersonalName");
		// authorRepository.save(author);

		initAuthors();
		initWorks();

	}
}
