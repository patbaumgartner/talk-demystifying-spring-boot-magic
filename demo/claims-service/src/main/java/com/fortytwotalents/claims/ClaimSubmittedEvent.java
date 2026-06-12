package com.fortytwotalents.claims;

import org.jmolecules.event.annotation.DomainEvent;

/**
 * Published when a new claim is submitted.
 *
 * <p>
 * This record lives at the root of the {@code claims} module and is therefore part of its
 * <em>public API</em>. Other modules (e.g. {@code audit}) may depend on it without
 * violating Spring Modulith's encapsulation rules.
 *
 * <p>
 * {@code @DomainEvent} (jMolecules) lets Spring Modulith's {@code Documenter} recognise
 * this type as a domain event and render it in the generated module canvas.
 *
 * @param claimId the ID of the newly created claim
 * @param principal the user who submitted the claim (from the X-User-Id header)
 */
@DomainEvent
public record ClaimSubmittedEvent(String claimId, String principal) {
}
