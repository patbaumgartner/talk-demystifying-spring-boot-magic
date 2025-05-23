package com.patbaumgartner.javafaker.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "javafaker")
public class JavaFakerProperties {

	/**
	 * Locale in the format of de-CH, higher precedence over language. See supported
	 * locales: https://github.com/DiUS/java-faker#supported-locales
	 */
	private String locale;

	/**
	 * language in the 2 character format like de.
	 */
	private String language;

}