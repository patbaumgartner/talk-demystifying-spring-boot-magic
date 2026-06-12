package com.fortytwotalents.claims.domain;

/**
 * Thrown when a claim with the requested ID does not exist. Domain exception – no
 * framework dependency.
 */
public class ClaimNotFoundException extends RuntimeException {

	public ClaimNotFoundException(String id) {
		super("Claim not found: " + id);
	}

}
