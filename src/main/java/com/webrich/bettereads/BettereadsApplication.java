package com.webrich.bettereads;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import com.webrich.bettereads.connection.DataStaxAstraProperties;
import com.webrich.bettereads.model.Author;
import com.webrich.bettereads.model.Book;
import com.webrich.bettereads.repository.AuthorRepository;
import com.webrich.bettereads.repository.BookRepository;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import io.netty.handler.codec.DateFormatter;

@SpringBootApplication
@EnableConfigurationProperties(DataStaxAstraProperties.class)
public class BettereadsApplication {

	@Autowired
	AuthorRepository authorRepository;

	@Autowired BookRepository bookRepository;

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
			lines.forEach(line -> {
				String jsonString = line.substring(line.indexOf("{"));
				try {
					JSONObject json = new JSONObject(jsonString);
					Author author = new Author();
					author.setId(json.optString("key").replace("/authors/", ""));
					author.setName(json.optString("name"));
					author.setPersonalName(json.optString("personal_name"));
					authorRepository.save(author);
					System.out.println("Author saved:"+author.getName());
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
		Path path = Paths.get(worksDumpLocation);
		try(Stream<String> lines =Files.lines(path)){
			lines.forEach(line->{
				try {

				String jsonString = line.substring(line.indexOf("{"));
				JSONObject json = new JSONObject(jsonString);
				Book book = new Book();
				book.setId(json.getString("key").replace("/works/", ""));
				String bookTitle = json.getString("title");
				book.setName(bookTitle);
				JSONObject description = json.optJSONObject("description");
				if(description!= null) book.setDescription(description.optString("value"));

				JSONArray coveArray = json.optJSONArray("covers");
				if(coveArray!=null) {
					List<String> coverIds = new ArrayList<String>();
					for(int i=0;i<coveArray.length();i++){
						coverIds.add(coveArray.getString(i));
					}
					book.setCoverIds(coverIds);
				}

				JSONArray authorArray = json.optJSONArray("authors");
				if(authorArray!=null){
					List<String> authorIds = new ArrayList<String>();
					for(int i=0;i<authorArray.length();i++){
						JSONObject authordIdObj = authorArray.getJSONObject(i);
						authorIds.add(authordIdObj.getJSONObject("author")
						.getString("key")
						.replace("/authors/", ""));

					}
					book.setAuthorIds(authorIds);
					List<String> authorNames = authorIds.stream().map(id->authorRepository.findById(id))
					.map(optAuthor->{
						if(!optAuthor.isPresent())return "Unknown Author";
						return optAuthor.get().getName();
					}).collect(Collectors.toList());
					book.setAuthorNames(authorNames);
				}

				JSONObject publishedJson = json.optJSONObject("created");
				if(publishedJson!=null){
					DateTimeFormatter dateFormat =DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
					String dateString = publishedJson.getString("value");
					book.setPublishedDate(LocalDate.parse(dateString,dateFormat));
				}
				bookRepository.save(book);
				System.out.println("Book saved:"+book.getName());

				} catch (Exception e) {e.printStackTrace();}
			}
			);


		} catch (Exception e) {
			//TODO: handle exception
		}

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
		System.out.println("DONE DONEDONEDONEDONEDONEDONEDONEDONEDONEDONEDONEDONEDONE");

	}
}
