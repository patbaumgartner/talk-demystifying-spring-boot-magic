package com.fortytwotalents.claims.application.service;

import com.fortytwotalents.claims.ClaimApprovedEvent;
import com.fortytwotalents.claims.ClaimRejectedEvent;
import com.fortytwotalents.claims.ClaimSubmittedEvent;
import com.fortytwotalents.claims.application.port.in.ClaimUseCase;
import com.fortytwotalents.claims.application.port.out.ClaimRepository;
import com.fortytwotalents.claims.domain.Claim;
import com.fortytwotalents.claims.domain.ClaimNotFoundException;
import com.fortytwotalents.claims.domain.ClaimStatus;
import org.jmolecules.architecture.hexagonal.Application;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Application service implementing {@link ClaimUseCase}.
 *
 * <p>
 * Orchestrates domain logic: persists claims via {@link ClaimRepository} and publishes
 * domain events ({@code ClaimSubmittedEvent}, etc.) for cross-module notifications via
 * the Spring Modulith JDBC event outbox (at-least-once delivery across restarts).
 *
 * <p>
 * Package-private – callers must use the {@link ClaimUseCase} port interface.
 */
@Application
@Service
@Transactional
class ClaimService implements ClaimUseCase {

	private final ClaimRepository repository;

	private final ApplicationEventPublisher events;

	ClaimService(ClaimRepository repository, ApplicationEventPublisher events) {
		this.repository = repository;
		this.events = events;
	}

	@Override
	@Transactional(readOnly = true)
	public List<Claim> findAll() {
		return repository.findAll();
	}

	@Override
	@Transactional(readOnly = true)
	public Claim findById(String id) {
		return repository.findById(id).orElseThrow(() -> new ClaimNotFoundException(id));
	}

	@Override
	public Claim submit(String principal, String type, String insuredId, String description) {
		Claim claim = Claim.create(type, ClaimStatus.SUBMITTED, insuredId);
		Claim saved = repository.create(claim);
		events.publishEvent(new ClaimSubmittedEvent(saved.id(), principal));
		return saved;
	}

	@Override
	public Claim approve(String claimId, String reviewerId) {
		Claim existing = findById(claimId);
		if (existing.status() != ClaimStatus.SUBMITTED) {
			throw new IllegalStateException(
					"Claim " + claimId + " cannot be approved from status " + existing.status());
		}
		Claim approved = repository.update(existing.withStatus(ClaimStatus.APPROVED));
		events.publishEvent(new ClaimApprovedEvent(claimId, reviewerId));
		return approved;
	}

	@Override
	public Claim reject(String claimId, String reviewerId) {
		Claim existing = findById(claimId);
		if (existing.status() != ClaimStatus.SUBMITTED) {
			throw new IllegalStateException(
					"Claim " + claimId + " cannot be rejected from status " + existing.status());
		}
		Claim rejected = repository.update(existing.withStatus(ClaimStatus.REJECTED));
		events.publishEvent(new ClaimRejectedEvent(claimId, reviewerId));
		return rejected;
	}

}
