package com.fortytwotalents.audit.autoconfigure;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Typed configuration properties for the audit starter.
 *
 * <p>
 * The on/off switch ({@code audit.enabled}) is handled at the auto-configuration level
 * via {@code @ConditionalOnBooleanProperty} and does not appear here.
 *
 * <p>
 * An explicitly blank {@code audit.topic} is rejected at startup by {@code @NotBlank}.
 *
 * <pre>
 * # application.properties
 * audit.topic=audit-events    # default – set only to override
 * audit.enabled=false         # disables all audit logging
 * </pre>
 *
 * @param topic logical channel / topic name written on every audit log line (default:
 * {@code "audit-events"})
 */
@ConfigurationProperties(prefix = "audit")
@Validated
public record AuditProperties(@NotBlank String topic) {

	/** Defaults {@code topic} to {@code "audit-events"} when the property is absent. */
	public AuditProperties {
		if (topic == null) {
			topic = "audit-events";
		}
	}

}
