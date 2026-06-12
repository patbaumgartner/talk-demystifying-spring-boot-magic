package com.fortytwotalents.audit.autoconfigure;

import com.fortytwotalents.audit.AuditEventListener;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for audit event logging.
 *
 * <p>
 * Active by default; set {@code audit.enabled=false} to disable entirely. Backs off
 * automatically if the consumer declares their own {@link AuditEventListener} bean, so
 * the default behaviour can be replaced without any extra wiring.
 *
 * <p>
 * Package-private – not part of the public API.
 */
@AutoConfiguration
@ConditionalOnClass(ApplicationEventPublisher.class)
@ConditionalOnBooleanProperty(prefix = "audit", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties(AuditProperties.class)
class AuditAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	AuditEventListener auditEventListener(AuditProperties properties) {
		return new AuditEventListener(properties.topic());
	}

}
