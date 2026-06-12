package com.fortytwotalents.claims.adapter.in.web;

import com.fortytwotalents.claims.domain.ClaimNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Translates domain exceptions into RFC 7807 {@link ProblemDetail} responses.
 *
 * <p>
 * Belongs to the web adapter – mapping exceptions to HTTP status codes is a transport
 * concern and must not leak into the application or domain layers.
 */
@RestControllerAdvice
class ClaimControllerAdvice {

	@ExceptionHandler(ClaimNotFoundException.class)
	ProblemDetail handleNotFound(ClaimNotFoundException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
		problem.setTitle("Claim Not Found");
		return problem;
	}

	@ExceptionHandler(IllegalStateException.class)
	ProblemDetail handleConflict(IllegalStateException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
		problem.setTitle("Invalid Claim State");
		return problem;
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
		problem.setTitle("Validation Error");
		problem.setProperty("errors",
				ex.getBindingResult()
					.getFieldErrors()
					.stream()
					.map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
					.toList());
		return problem;
	}

}
