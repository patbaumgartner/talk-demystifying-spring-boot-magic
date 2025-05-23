package com.patbaumgartner.javafaker.autoconfigure;

import com.github.javafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

import java.util.Locale;

@AutoConfiguration
@ConditionalOnClass(Faker.class)
@EnableConfigurationProperties(JavaFakerProperties.class)
public class JavaFakerAutoConfiguration {

	@Autowired
	private JavaFakerProperties fakerProperties;

	@Bean
	@ConditionalOnMissingBean
	public Faker faker() {

		if (StringUtils.hasText(fakerProperties.getLocale())) {
			return Faker.instance(new Locale(fakerProperties.getLocale()));
		}

		if (StringUtils.hasText(fakerProperties.getLanguage())) {
			return Faker.instance(new Locale(fakerProperties.getLanguage()));
		}

		return Faker.instance();

	}

}
