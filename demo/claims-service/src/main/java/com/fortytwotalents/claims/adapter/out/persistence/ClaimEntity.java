package com.fortytwotalents.claims.adapter.out.persistence;

import com.fortytwotalents.claims.domain.Claim;
import com.fortytwotalents.claims.domain.ClaimStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Spring Data JDBC persistence entity – internal to the persistence adapter. Kept
 * separate from the domain {@link Claim} record so infrastructure annotations never leak
 * into the domain.
 *
 * <p>
 * Implements {@link Persistable} so Spring Data JDBC calls {@link #isNew()} rather than
 * checking for a null ID; necessary because UUIDs are pre-assigned before the first
 * INSERT. Use {@link #forInsert} for new claims and {@link #forUpdate} for state
 * transitions.
 */
@Table("claims")
class ClaimEntity implements Persistable<String> {

	@Id
	private final String id;

	private final String type;

	private final ClaimStatus status;

	private final String insuredId;

	/** Not mapped to a column; excluded from JSON serialisation as a safety measure. */
	@Transient
	private final boolean newEntity;

	/** Used by Spring Data JDBC to reconstruct entities from database rows. */
	@PersistenceCreator
	ClaimEntity(String id, String type, ClaimStatus status, String insuredId) {
		this(id, type, status, insuredId, false);
	}

	private ClaimEntity(String id, String type, ClaimStatus status, String insuredId, boolean newEntity) {
		this.id = id;
		this.type = type;
		this.status = status;
		this.insuredId = insuredId;
		this.newEntity = newEntity;
	}

	/** Creates an entity flagged for INSERT (brand-new claim). */
	static ClaimEntity forInsert(Claim claim) {
		return new ClaimEntity(claim.id(), claim.type(), claim.status(), claim.insuredId(), true);
	}

	/** Creates an entity for UPDATE (state transition on an existing claim). */
	static ClaimEntity forUpdate(Claim claim) {
		return new ClaimEntity(claim.id(), claim.type(), claim.status(), claim.insuredId(), false);
	}

	/** Maps back to the domain object. */
	Claim toDomain() {
		return new Claim(id, type, status, insuredId);
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public boolean isNew() {
		return newEntity;
	}

	// Getters required by Spring Data JDBC for column read-back after save
	public String getType() {
		return type;
	}

	public ClaimStatus getStatus() {
		return status;
	}

	public String getInsuredId() {
		return insuredId;
	}

}
