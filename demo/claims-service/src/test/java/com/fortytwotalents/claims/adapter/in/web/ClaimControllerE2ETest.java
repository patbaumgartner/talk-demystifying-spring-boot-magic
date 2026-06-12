package com.fortytwotalents.claims.adapter.in.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Full end-to-end HTTP tests for {@link ClaimController}.
 *
 * <p>
 * Boots the complete Spring context against a real PostgreSQL instance managed by
 * Testcontainers. {@code @ServiceConnection} wires the container into Spring Boot's
 * DataSource auto-configuration without manual property overrides.
 *
 * <p>
 * Covers every scenario from Request.http: happy-path CRUD, approve/reject transitions,
 * conflict (409) detection, and validation error (400) responses.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = "spring.docker.compose.enabled=false")
@ActiveProfiles("test")
@AutoConfigureTestRestTemplate
@Testcontainers(disabledWithoutDocker = true)
class ClaimControllerE2ETest {

	@Container
	@ServiceConnection
	static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:17-alpine"));

	@Autowired
	TestRestTemplate rest;

	@Autowired
	JdbcTemplate jdbc;

	private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE = new ParameterizedTypeReference<>() {
	};

	private static final ParameterizedTypeReference<List<Map<String, Object>>> LIST_TYPE = new ParameterizedTypeReference<>() {
	};

	@BeforeEach
	void setUp() {
		jdbc.execute("DELETE FROM event_publication");
		jdbc.execute("DELETE FROM claims");
	}

	// helpers

	private HttpEntity<String> jsonEntity(String body) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-User-Id", "user:42");
		return new HttpEntity<>(body, headers);
	}

	private HttpEntity<Void> reviewerEntity() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("X-User-Id", "reviewer:7");
		return new HttpEntity<>(null, headers);
	}

	private String submitClaim(String type, String insuredId, String description) {
		String body = String.format("{\"type\":\"%s\",\"insuredId\":\"%s\",\"description\":\"%s\"}", type, insuredId,
				description);
		ResponseEntity<Map<String, Object>> response = rest.exchange("/claims", HttpMethod.POST, jsonEntity(body),
				MAP_TYPE);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).containsKey("id");
		return (String) response.getBody().get("id");
	}

	// Request 1 - POST /claims -> 201 CREATED
	@Test
	void submitClaim_returns201AndClaim() {
		String body = "{\"type\":\"DENTAL\",\"insuredId\":\"ins-1\",\"description\":\"Annual check-up\"}";
		ResponseEntity<Map<String, Object>> response = rest.exchange("/claims", HttpMethod.POST, jsonEntity(body),
				MAP_TYPE);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).containsKey("id")
			.containsEntry("type", "DENTAL")
			.containsEntry("status", "SUBMITTED")
			.containsEntry("insuredId", "ins-1");
	}

	// Request 2 - GET /claims -> 200 OK with list
	@Test
	void listClaims_returns200AndArray() {
		submitClaim("DENTAL", "ins-1", "check-up");
		submitClaim("VISION", "ins-2", "contact lenses");

		ResponseEntity<List<Map<String, Object>>> response = rest.exchange("/claims", HttpMethod.GET, null, LIST_TYPE);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).hasSize(2);
	}

	// Request 3 - GET /claims/{id} -> 200 OK, status = SUBMITTED
	@Test
	void getClaim_returnsSubmittedClaim() {
		String id = submitClaim("DENTAL", "ins-1", "check-up");

		ResponseEntity<Map<String, Object>> response = rest.exchange("/claims/" + id, HttpMethod.GET, null, MAP_TYPE);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).containsEntry("id", id).containsEntry("status", "SUBMITTED");
	}

	// Request 4 - PUT /claims/{id}/approve -> 200 OK, status = APPROVED
	@Test
	void approveClaim_returnsApprovedClaim() {
		String id = submitClaim("DENTAL", "ins-1", "check-up");

		ResponseEntity<Map<String, Object>> response = rest.exchange("/claims/" + id + "/approve", HttpMethod.PUT,
				reviewerEntity(), MAP_TYPE);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).containsEntry("status", "APPROVED");
	}

	// Request 5 - PUT /claims/{id}/approve (again) -> 409 CONFLICT
	@Test
	void approveAlreadyApprovedClaim_returns409() {
		String id = submitClaim("DENTAL", "ins-1", "check-up");

		rest.exchange("/claims/" + id + "/approve", HttpMethod.PUT, reviewerEntity(), MAP_TYPE);
		ResponseEntity<Map<String, Object>> response = rest.exchange("/claims/" + id + "/approve", HttpMethod.PUT,
				reviewerEntity(), MAP_TYPE);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
	}

	// Request 6 - POST /claims without type -> 400 BAD REQUEST
	@Test
	void submitClaimWithoutType_returns400() {
		String body = "{\"insuredId\":\"ins-2\",\"description\":\"Missing type\"}";
		ResponseEntity<Map<String, Object>> response = rest.exchange("/claims", HttpMethod.POST, jsonEntity(body),
				MAP_TYPE);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).containsEntry("title", "Validation Error");
		List<?> errors = (List<?>) response.getBody().get("errors");
		assertThat(errors).anyMatch(e -> e.toString().contains("type"));
	}

	// Request 7 - POST /claims without insuredId -> 400 BAD REQUEST
	@Test
	void submitClaimWithoutInsuredId_returns400() {
		String body = "{\"type\":\"VISION\",\"description\":\"Missing insuredId\"}";
		ResponseEntity<Map<String, Object>> response = rest.exchange("/claims", HttpMethod.POST, jsonEntity(body),
				MAP_TYPE);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).containsEntry("title", "Validation Error");
		List<?> errors = (List<?>) response.getBody().get("errors");
		assertThat(errors).anyMatch(e -> e.toString().contains("insuredId"));
	}

	// Request 8 - GET /claims/{nonExistentId} -> 404 NOT FOUND
	@Test
	void getNonExistentClaim_returns404() {
		ResponseEntity<Map<String, Object>> response = rest.exchange("/claims/00000000-0000-0000-0000-000000000000",
				HttpMethod.GET, null, MAP_TYPE);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).containsEntry("title", "Claim Not Found");
	}

	// Request 9+10 - POST then PUT /reject -> 200 OK, status = REJECTED
	@Test
	void rejectClaim_returnsRejectedClaim() {
		String id = submitClaim("VISION", "ins-2", "Contact lenses");

		ResponseEntity<Map<String, Object>> response = rest.exchange("/claims/" + id + "/reject", HttpMethod.PUT,
				reviewerEntity(), MAP_TYPE);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).containsEntry("status", "REJECTED");
	}

	// Request 11 - PUT /reject on already-rejected claim -> 409 CONFLICT
	@Test
	void rejectAlreadyRejectedClaim_returns409() {
		String id = submitClaim("VISION", "ins-2", "Contact lenses");

		rest.exchange("/claims/" + id + "/reject", HttpMethod.PUT, reviewerEntity(), MAP_TYPE);
		ResponseEntity<Map<String, Object>> response = rest.exchange("/claims/" + id + "/reject", HttpMethod.PUT,
				reviewerEntity(), MAP_TYPE);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
	}

	// Full happy-path flow
	@Test
	void fullHappyPath_submitListGetApprove() {
		String id = submitClaim("DENTAL", "ins-1", "Annual check-up");

		List<Map<String, Object>> list = rest.exchange("/claims", HttpMethod.GET, null, LIST_TYPE).getBody();
		assertThat(list).hasSize(1);

		Map<String, Object> claim = rest.exchange("/claims/" + id, HttpMethod.GET, null, MAP_TYPE).getBody();
		assertThat(claim).containsEntry("status", "SUBMITTED");

		ResponseEntity<Map<String, Object>> approved = rest.exchange("/claims/" + id + "/approve", HttpMethod.PUT,
				reviewerEntity(), MAP_TYPE);
		assertThat(approved.getBody()).containsEntry("status", "APPROVED");

		ResponseEntity<Map<String, Object>> conflict = rest.exchange("/claims/" + id + "/approve", HttpMethod.PUT,
				reviewerEntity(), MAP_TYPE);
		assertThat(conflict.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
	}

}
