package com.fortytwotalents.claims.application.service;

import com.fortytwotalents.claims.ClaimApprovedEvent;
import com.fortytwotalents.claims.ClaimRejectedEvent;
import com.fortytwotalents.claims.ClaimSubmittedEvent;
import com.fortytwotalents.claims.application.port.out.ClaimRepository;
import com.fortytwotalents.claims.domain.Claim;
import com.fortytwotalents.claims.domain.ClaimNotFoundException;
import com.fortytwotalents.claims.domain.ClaimStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

	@Mock
	ClaimRepository repository;

	@Mock
	ApplicationEventPublisher eventPublisher;

	@InjectMocks
	ClaimService service;

	@Test
	void findAllDelegatesToRepository() {
		Claim claim = new Claim("c-1", "DENTAL", ClaimStatus.SUBMITTED, "ins-1");
		when(repository.findAll()).thenReturn(List.of(claim));
		assertThat(service.findAll()).containsExactly(claim);
	}

	@Test
	void findByIdThrowsWhenClaimMissing() {
		when(repository.findById("missing")).thenReturn(Optional.empty());
		assertThatThrownBy(() -> service.findById("missing")).isInstanceOf(ClaimNotFoundException.class)
			.hasMessageContaining("missing");
	}

	@Test
	void submitCreatesClaimWithStatusSubmitted() {
		when(repository.create(any())).thenAnswer(inv -> inv.getArgument(0));
		Claim result = service.submit("user:42", "DENTAL", "ins-1", "check-up");
		assertThat(result.type()).isEqualTo("DENTAL");
		assertThat(result.insuredId()).isEqualTo("ins-1");
		assertThat(result.status()).isEqualTo(ClaimStatus.SUBMITTED);
		assertThat(result.id()).isNotBlank();
	}

	@Test
	void submitPublishesClaimSubmittedEvent() {
		when(repository.create(any())).thenAnswer(inv -> inv.getArgument(0));
		Claim result = service.submit("user:42", "DENTAL", "ins-1", "check-up");
		verify(eventPublisher).publishEvent(new ClaimSubmittedEvent(result.id(), "user:42"));
	}

	@Test
	void approveTransitionsStatusToApproved() {
		Claim submitted = new Claim("c-1", "DENTAL", ClaimStatus.SUBMITTED, "ins-1");
		when(repository.findById("c-1")).thenReturn(Optional.of(submitted));
		when(repository.update(any())).thenAnswer(inv -> inv.getArgument(0));
		Claim result = service.approve("c-1", "reviewer:7");
		assertThat(result.status()).isEqualTo(ClaimStatus.APPROVED);
		verify(eventPublisher).publishEvent(new ClaimApprovedEvent("c-1", "reviewer:7"));
	}

	@Test
	void approveThrowsWhenClaimAlreadyApproved() {
		Claim approved = new Claim("c-1", "DENTAL", ClaimStatus.APPROVED, "ins-1");
		when(repository.findById("c-1")).thenReturn(Optional.of(approved));
		assertThatThrownBy(() -> service.approve("c-1", "reviewer:7")).isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("cannot be approved from status APPROVED");
	}

	@Test
	void rejectTransitionsStatusToRejected() {
		Claim submitted = new Claim("c-1", "DENTAL", ClaimStatus.SUBMITTED, "ins-1");
		when(repository.findById("c-1")).thenReturn(Optional.of(submitted));
		when(repository.update(any())).thenAnswer(inv -> inv.getArgument(0));
		Claim result = service.reject("c-1", "reviewer:7");
		assertThat(result.status()).isEqualTo(ClaimStatus.REJECTED);
		verify(eventPublisher).publishEvent(new ClaimRejectedEvent("c-1", "reviewer:7"));
	}

	@Test
	void rejectThrowsWhenClaimAlreadyRejected() {
		Claim rejected = new Claim("c-1", "DENTAL", ClaimStatus.REJECTED, "ins-1");
		when(repository.findById("c-1")).thenReturn(Optional.of(rejected));
		assertThatThrownBy(() -> service.reject("c-1", "reviewer:7")).isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("cannot be rejected from status REJECTED");
	}

}
