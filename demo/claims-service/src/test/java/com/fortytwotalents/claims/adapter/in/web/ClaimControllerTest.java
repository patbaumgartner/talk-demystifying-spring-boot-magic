package com.fortytwotalents.claims.adapter.in.web;

import com.fortytwotalents.claims.application.port.in.ClaimUseCase;
import com.fortytwotalents.claims.domain.Claim;
import com.fortytwotalents.claims.domain.ClaimNotFoundException;
import com.fortytwotalents.claims.domain.ClaimStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@code @WebMvcTest} slice test for {@link ClaimController}. Only the web layer is
 * loaded; {@link ClaimUseCase} is mocked at the port boundary.
 */
@WebMvcTest(ClaimController.class)
class ClaimControllerTest {

	@Autowired
	MockMvc mvc;

	@MockitoBean
	ClaimUseCase useCase;

	// ── Read operations ───────────────────────────────────────────────────────

	@Test
	void listReturnsAllClaims() throws Exception {
		when(useCase.findAll()).thenReturn(List.of(new Claim("c-1", "DENTAL", ClaimStatus.SUBMITTED, "ins-1")));

		mvc.perform(get("/claims"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].id").value("c-1"))
			.andExpect(jsonPath("$[0].status").value("SUBMITTED"));
	}

	@Test
	void getReturns404WhenNotFound() throws Exception {
		when(useCase.findById("missing")).thenThrow(new ClaimNotFoundException("missing"));

		mvc.perform(get("/claims/missing")).andExpect(status().isNotFound());
	}

	// ── Submit ────────────────────────────────────────────────────────────────

	@Test
	void submitCreatesClaimAndReturns201() throws Exception {
		Claim created = new Claim("c-2", "DENTAL", ClaimStatus.SUBMITTED, "ins-2");
		when(useCase.submit(eq("user:42"), eq("DENTAL"), eq("ins-2"), any())).thenReturn(created);

		mvc.perform(post("/claims").header("X-User-Id", "user:42").contentType(MediaType.APPLICATION_JSON).content("""
				{"type":"DENTAL","insuredId":"ins-2","description":"Filling"}
				"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").value("c-2"))
			.andExpect(jsonPath("$.status").value("SUBMITTED"));
	}

	@Test
	void submitReturns400WhenTypeIsMissing() throws Exception {
		mvc.perform(post("/claims").header("X-User-Id", "user:42").contentType(MediaType.APPLICATION_JSON).content("""
				{"insuredId":"ins-2","description":"Filling"}
				""")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.title").value("Validation Error"));
	}

	@Test
	void submitReturns400WhenInsuredIdIsMissing() throws Exception {
		mvc.perform(post("/claims").header("X-User-Id", "user:42").contentType(MediaType.APPLICATION_JSON).content("""
				{"type":"DENTAL","description":"Filling"}
				""")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.title").value("Validation Error"));
	}

	// ── Approve / Reject ──────────────────────────────────────────────────────

	@Test
	void approveChangesStatusToApproved() throws Exception {
		Claim approved = new Claim("c-1", "DENTAL", ClaimStatus.APPROVED, "ins-1");
		when(useCase.approve("c-1", "reviewer:7")).thenReturn(approved);

		mvc.perform(put("/claims/c-1/approve").header("X-User-Id", "reviewer:7"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("APPROVED"));
	}

	@Test
	void rejectChangesStatusToRejected() throws Exception {
		Claim rejected = new Claim("c-1", "DENTAL", ClaimStatus.REJECTED, "ins-1");
		when(useCase.reject("c-1", "reviewer:7")).thenReturn(rejected);

		mvc.perform(put("/claims/c-1/reject").header("X-User-Id", "reviewer:7"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("REJECTED"));
	}

	@Test
	void approveReturns409WhenClaimAlreadyApproved() throws Exception {
		when(useCase.approve(eq("c-1"), any()))
			.thenThrow(new IllegalStateException("Claim c-1 cannot be approved from status APPROVED"));

		mvc.perform(put("/claims/c-1/approve").header("X-User-Id", "reviewer:7"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.title").value("Invalid Claim State"));
	}

	@Test
	void rejectReturns409WhenClaimAlreadyRejected() throws Exception {
		when(useCase.reject(eq("c-1"), any()))
			.thenThrow(new IllegalStateException("Claim c-1 cannot be rejected from status REJECTED"));

		mvc.perform(put("/claims/c-1/reject").header("X-User-Id", "reviewer:7"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.title").value("Invalid Claim State"));
	}

}
