package com.fortytwotalents.claims;

import org.jmolecules.event.annotation.DomainEvent;

/**
 * Published when a submitted claim is rejected by a reviewer.
 *
 * <p>
 * Module public API – may be consumed by other Spring Modulith modules.
 *
 * <p>
 * {@code @DomainEvent} (jMolecules) lets Spring Modulith's {@code Documenter} recognise
 * this type as a domain event and render it in the generated module canvas.
 *
 * @param claimId the ID of the rejected claim
 * @param reviewerId the reviewer who rejected the claim (from the X-User-Id header)
 */
@DomainEvent
public record ClaimRejectedEvent(String claimId, String reviewerId) {
}
