package com.fortytwotalents.claims;

import org.jmolecules.event.annotation.DomainEvent;

/**
 * Published when a submitted claim is approved by a reviewer.
 *
 * <p>
 * Module public API – may be consumed by other Spring Modulith modules.
 *
 * <p>
 * {@code @DomainEvent} (jMolecules) lets Spring Modulith's {@code Documenter} recognise
 * this type as a domain event and render it in the generated module canvas.
 *
 * @param claimId the ID of the approved claim
 * @param reviewerId the reviewer who approved the claim (from the X-User-Id header)
 */
@DomainEvent
public record ClaimApprovedEvent(String claimId, String reviewerId) {
}
