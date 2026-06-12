package com.fortytwotalents.claims.application.port.in;

import com.fortytwotalents.claims.domain.Claim;
import org.jmolecules.architecture.hexagonal.PrimaryPort;

import java.util.List;

/**
 * Inbound port (driving side) – defines the use cases the application exposes.
 *
 * <p>
 * The web adapter calls this interface; it never references the service implementation
 * directly. This means the REST layer can be swapped for a gRPC adapter, a CLI adapter,
 * or a message-driven adapter without touching any business logic.
 *
 * <p>
 * In hexagonal architecture this interface sits at the boundary between the
 * <em>application</em> ring and the <em>driving adapters</em> ring.
 */
@PrimaryPort
public interface ClaimUseCase {

	List<Claim> findAll();

	Claim findById(String id);

	/**
	 * Submits a new claim on behalf of {@code principal}. {@code description} is
	 * informational only and is not persisted.
	 */
	Claim submit(String principal, String type, String insuredId, String description);

	Claim approve(String claimId, String reviewerId);

	Claim reject(String claimId, String reviewerId);

}
