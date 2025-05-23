package com.patbaumgartner.javafakerapplication;

import lombok.extern.slf4j.Slf4j;
import com.github.javafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class JavafakerApplication implements CommandLineRunner {

	@Autowired
	private Faker faker;

	public static void main(String[] args) {
		SpringApplication.run(JavafakerApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		String message = faker.artist().name();
		log.info("***");
		log.info("Choosing the right name is difficult, lets give JavaFaker a try.");
		log.info("And the winner is: {}", message);
		log.info("***");
	}

}
