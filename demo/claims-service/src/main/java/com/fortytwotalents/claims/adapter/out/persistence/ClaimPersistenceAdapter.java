package com.fortytwotalents.claims.adapter.out.persistence;

import com.fortytwotalents.claims.application.port.out.ClaimRepository;
import com.fortytwotalents.claims.domain.Claim;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

/**
 * Outbound persistence adapter – implements {@link ClaimRepository} using Spring Data
 * JDBC. Domain/persistence mapping is isolated here; neither the domain nor the
 * application layer references this class directly.
 */
@SecondaryAdapter
@Repository
class ClaimPersistenceAdapter implements ClaimRepository {

	private final JdbcClaimRepository jdbcRepository;

	ClaimPersistenceAdapter(JdbcClaimRepository jdbcRepository) {
		this.jdbcRepository = jdbcRepository;
	}

	@Override
	public List<Claim> findAll() {
		return StreamSupport.stream(jdbcRepository.findAll().spliterator(), false).map(ClaimEntity::toDomain).toList();
	}

	@Override
	public Optional<Claim> findById(String id) {
		return jdbcRepository.findById(id).map(ClaimEntity::toDomain);
	}

	@Override
	public Claim create(Claim claim) {
		return jdbcRepository.save(ClaimEntity.forInsert(claim)).toDomain();
	}

	@Override
	public Claim update(Claim claim) {
		return jdbcRepository.save(ClaimEntity.forUpdate(claim)).toDomain();
	}

}
