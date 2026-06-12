package com.fortytwotalents.audit;

/**
 * Describes an auditable action that occurred in the system.
 *
 * <p>
 * Publish via Spring's {@link org.springframework.context.ApplicationEventPublisher} to
 * trigger the auto-configured {@link AuditEventListener}.
 *
 * @param principal the user or service that triggered the action (e.g. {@code "user:42"},
 * {@code "system"})
 * @param action what happened (e.g. {@code "CLAIM_APPROVED"}, {@code "RECORD_DELETED"})
 * @param resourceId the unique identifier of the affected resource
 */
public record AuditEvent(String principal, String action, String resourceId) {
}
