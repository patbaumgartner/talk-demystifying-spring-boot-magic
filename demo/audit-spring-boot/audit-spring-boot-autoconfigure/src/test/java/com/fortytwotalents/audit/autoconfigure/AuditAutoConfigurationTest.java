package com.fortytwotalents.audit.autoconfigure;

import com.fortytwotalents.audit.AuditEvent;
import com.fortytwotalents.audit.AuditEventListener;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.validation.autoconfigure.ValidationAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link AuditAutoConfiguration} using {@link ApplicationContextRunner}.
 *
 * <p>
 * Each test method creates an isolated
 * {@link org.springframework.context.ApplicationContext} that precisely reflects the
 * scenario under test – no shared state between tests.
 */
class AuditAutoConfigurationTest {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(ValidationAutoConfiguration.class, AuditAutoConfiguration.class));

	@Test
	void registersAuditEventListenerByDefault() {
		contextRunner.run(ctx -> {
			assertThat(ctx).hasSingleBean(AuditEventListener.class);
			assertThat(ctx).hasSingleBean(AuditProperties.class);
		});
	}

	@Test
	void defaultTopicIsAuditEvents() {
		contextRunner.run(ctx -> {
			AuditProperties props = ctx.getBean(AuditProperties.class);
			assertThat(props.topic()).isEqualTo("audit-events");
		});
	}

	@Test
	void customTopicIsHonoured() {
		contextRunner.withPropertyValues("audit.topic=claims-audit").run(ctx -> {
			AuditProperties props = ctx.getBean(AuditProperties.class);
			assertThat(props.topic()).isEqualTo("claims-audit");
		});
	}

	@Test
	void disabledWhenPropertySetToFalse() {
		contextRunner.withPropertyValues("audit.enabled=false")
			.run(ctx -> assertThat(ctx).doesNotHaveBean(AuditEventListener.class));
	}

	@Test
	void backsOffWhenUserDefinesOwnListener() {
		contextRunner.withUserConfiguration(CustomListenerConfig.class).run(ctx -> {
			assertThat(ctx).hasSingleBean(AuditEventListener.class);
			assertThat(ctx.getBeanNamesForType(AuditEventListener.class)).containsExactly("customListener");
		});
	}

	@Test
	void validationFailsWhenTopicIsExplicitlyBlank() {
		contextRunner.withPropertyValues("audit.topic= ").run(ctx -> assertThat(ctx).hasFailed());
	}

	@Test
	void publishedEventIsHandledWithoutError() {
		contextRunner.run(ctx -> ctx.getSourceApplicationContext()
			.publishEvent(new AuditEvent("user:1", "TEST_ACTION", "resource:99")));
	}

	@Configuration
	static class CustomListenerConfig {

		@Bean
		AuditEventListener customListener() {
			return new AuditEventListener("custom-topic");
		}

	}

}
