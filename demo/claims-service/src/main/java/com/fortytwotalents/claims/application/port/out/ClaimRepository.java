package com.fortytwotalents.claims.application.port.out;

import com.fortytwotalents.claims.domain.Claim;
import org.jmolecules.architecture.hexagonal.SecondaryPort;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port (driven side) – persistence contract defined by the application.
 *
 * <p>
 * The application service depends on this interface, never on a concrete JDBC or JPA
 * class. Separating {@code create} from {@code update} makes the intent explicit and
 * avoids the extra "does this row exist?" query that a generic {@code save()} would need.
 *
 * <p>
 * The Spring Data JDBC adapter in {@code adapter.out.persistence} implements this
 * interface and is wired in automatically by Spring Boot.
 */
@SecondaryPort
public interface ClaimRepository {

	List<Claim> findAll();

	Optional<Claim> findById(String id);

	/** Inserts a brand-new claim. Must be called exactly once per claim lifecycle. */
	Claim create(Claim claim);

	/** Updates an existing claim (state transition). */
	Claim update(Claim claim);

}
