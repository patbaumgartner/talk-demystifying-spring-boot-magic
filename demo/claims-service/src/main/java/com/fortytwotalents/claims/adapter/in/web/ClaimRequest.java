package com.fortytwotalents.claims.adapter.in.web;

import jakarta.validation.constraints.NotBlank;

/**
 * HTTP request DTO for submitting a new claim.
 *
 * <p>
 * Belongs to the web adapter, not the domain: validation annotations ({@code @NotBlank})
 * are HTTP-layer concerns and must not appear on domain objects.
 */
public record ClaimRequest(@NotBlank(message = "type is required") String type,
		@NotBlank(message = "insuredId is required") String insuredId, String description) {
}
