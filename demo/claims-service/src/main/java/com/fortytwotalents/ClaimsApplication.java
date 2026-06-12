package com.fortytwotalents;

import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;

/**
 * Entry point for the Claims Service.
 *
 * <p>
 * This application intentionally declares <em>no</em> audit beans. The
 * {@code audit-spring-boot-starter} dependency auto-configures
 * {@code AuditEventListener}
 * and {@code AuditProperties} automatically based on what is on the classpath –
 * a live
 * demonstration of Spring Boot auto-configuration.
 *
 * <p>
 * Run with {@code ./mvnw spring-boot:run} (requires Docker for PostgreSQL), or
 * run the
 * integration tests directly – Testcontainers handles the database lifecycle
 * via
 * {@code @ServiceConnection}.
 */
@SpringBootApplication
public class ClaimsApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClaimsApplication.class, args);
	}

	/**
	 * Wires the Logback {@link OpenTelemetryAppender} to the Spring-managed
	 * {@link OpenTelemetry} SDK instance.
	 * <p>
	 * Spring Boot 4.1 creates its own {@code OpenTelemetrySdk} bean rather than
	 * registering it as the global instance, so the appender (which defaults to
	 * {@code GlobalOpenTelemetry}) must be installed explicitly once the context
	 * is fully started.
	 * <p>
	 * {@code @ConditionalOnBean} keeps this out of {@code @WebMvcTest} slices
	 * where the full OTel SDK is not loaded.
	 */
	@Bean
	ApplicationListener<ApplicationStartedEvent> openTelemetryAppenderInstaller(
			ObjectProvider<OpenTelemetrySdk> openTelemetryProvider) {
		return event -> {
			OpenTelemetrySdk openTelemetry = openTelemetryProvider.getIfAvailable();
			if (openTelemetry != null) {
				OpenTelemetryAppender.install(openTelemetry);
			}
		};
	}

}
