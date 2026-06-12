package com.fortytwotalents.claims.domain;

import java.util.UUID;

/**
 * Core domain entity – a health insurance claim.
 *
 * <p>
 * This is plain Java with no framework annotations. The hexagonal architecture rule that
 * matters here: <em>the domain knows nothing about Spring, JDBC, HTTP, or any other
 * infrastructure concern.</em>
 *
 * <p>
 * UUID assignment lives here because it is a domain decision (every claim gets a unique
 * identity at creation time), not a persistence or web concern.
 */
public record Claim(String id, String type, ClaimStatus status, String insuredId) {

	/**
	 * Factory method for brand-new claims. Assigns a UUID before the claim is persisted
	 * for the first time.
	 */
	public static Claim create(String type, ClaimStatus status, String insuredId) {
		return new Claim(UUID.randomUUID().toString(), type, status, insuredId);
	}

	/**
	 * Returns a copy of this claim with the given status applied. Used for approve /
	 * reject state transitions; the original instance is unchanged.
	 */
	public Claim withStatus(ClaimStatus newStatus) {
		return new Claim(id, type, newStatus, insuredId);
	}

}
