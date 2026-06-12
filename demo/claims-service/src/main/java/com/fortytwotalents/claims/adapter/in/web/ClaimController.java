package com.fortytwotalents.claims.adapter.in.web;

import com.fortytwotalents.claims.application.port.in.ClaimUseCase;
import com.fortytwotalents.claims.domain.Claim;
import jakarta.validation.Valid;
import org.jmolecules.architecture.hexagonal.PrimaryAdapter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Inbound web adapter – translates HTTP requests into {@link ClaimUseCase} calls.
 *
 * <p>
 * All dependencies are through the {@link ClaimUseCase} port interface; this class never
 * references service or persistence implementations directly. Audit and persistence
 * concerns are handled transparently behind the port.
 *
 * <p>
 * Error responses follow RFC 7807 Problem Details; see {@link ClaimControllerAdvice}.
 */
@PrimaryAdapter
@RestController
@RequestMapping("/claims")
public class ClaimController {

	private final ClaimUseCase useCase;

	public ClaimController(ClaimUseCase useCase) {
		this.useCase = useCase;
	}

	@GetMapping
	public List<Claim> list() {
		return useCase.findAll();
	}

	@GetMapping("/{id}")
	public Claim get(@PathVariable String id) {
		return useCase.findById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Claim submit(@RequestHeader(value = "X-User-Id", defaultValue = "anonymous") String userId,
			@Valid @RequestBody ClaimRequest request) {
		return useCase.submit(userId, request.type(), request.insuredId(), request.description());
	}

	@PutMapping("/{id}/approve")
	public Claim approve(@PathVariable String id,
			@RequestHeader(value = "X-User-Id", defaultValue = "anonymous") String reviewerId) {
		return useCase.approve(id, reviewerId);
	}

	@PutMapping("/{id}/reject")
	public Claim reject(@PathVariable String id,
			@RequestHeader(value = "X-User-Id", defaultValue = "anonymous") String reviewerId) {
		return useCase.reject(id, reviewerId);
	}

}
