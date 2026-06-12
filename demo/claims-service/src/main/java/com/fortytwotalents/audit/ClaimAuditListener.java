package com.fortytwotalents.audit;

import com.fortytwotalents.claims.ClaimApprovedEvent;
import com.fortytwotalents.claims.ClaimRejectedEvent;
import com.fortytwotalents.claims.ClaimSubmittedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Translates {@code claims} domain events into {@link AuditEvent}s and forwards them to
 * the auto-configured {@link AuditEventListener}.
 *
 * <p>
 * Handlers are annotated with {@code @ApplicationModuleListener} so they run
 * <em>after</em> the originating transaction commits. The JDBC event outbox guarantees
 * at-least-once delivery: if the process restarts before a handler completes, the event
 * is re-delivered on the next startup.
 */
@Component
class ClaimAuditListener {

	private final ApplicationEventPublisher publisher;

	ClaimAuditListener(ApplicationEventPublisher publisher) {
		this.publisher = publisher;
	}

	@ApplicationModuleListener
	void on(ClaimSubmittedEvent event) {
		publisher.publishEvent(new AuditEvent(event.principal(), "CLAIM_SUBMITTED", event.claimId()));
	}

	@ApplicationModuleListener
	void on(ClaimApprovedEvent event) {
		publisher.publishEvent(new AuditEvent(event.reviewerId(), "CLAIM_APPROVED", event.claimId()));
	}

	@ApplicationModuleListener
	void on(ClaimRejectedEvent event) {
		publisher.publishEvent(new AuditEvent(event.reviewerId(), "CLAIM_REJECTED", event.claimId()));
	}

}
