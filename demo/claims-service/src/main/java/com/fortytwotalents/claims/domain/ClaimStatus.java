package com.fortytwotalents.claims.domain;

/**
 * Lifecycle states of a health insurance claim.
 *
 * <pre>
 *   SUBMITTED ──► APPROVED
 *   SUBMITTED ──► REJECTED
 * </pre>
 *
 * APPROVED and REJECTED are terminal – no further transitions are allowed.
 */
public enum ClaimStatus {

	/** Claim received and awaiting review. */
	SUBMITTED,

	/** Claim accepted by a reviewer. */
	APPROVED,

	/** Claim denied by a reviewer. */
	REJECTED

}
