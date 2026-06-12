package com.fortytwotalents.claims;

import com.fortytwotalents.audit.AuditEventListener;
import com.fortytwotalents.audit.autoconfigure.AuditProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.ApplicationContext;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Full Spring Boot context integration test backed by a Testcontainers PostgreSQL
 * instance. {@code @ServiceConnection} wires the container into DataSource
 * auto-configuration without manual property overrides.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = "spring.docker.compose.enabled=false")
@Testcontainers(disabledWithoutDocker = true)
class ClaimsApplicationTest {

	@Container
	@ServiceConnection
	static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:17-alpine"));

	@Autowired
	ApplicationContext context;

	@Test
	void contextLoads() {
		assertThat(context).isNotNull();
	}

	@Test
	void auditListenerIsAutoConfigured() {
		assertThat(context.getBeansOfType(AuditEventListener.class)).isNotEmpty();
	}

	@Test
	void defaultAuditTopicIsAuditEvents() {
		AuditProperties props = context.getBean(AuditProperties.class);
		assertThat(props.topic()).isEqualTo("audit-events");
	}

}
