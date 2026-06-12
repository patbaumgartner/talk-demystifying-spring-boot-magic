/**
 * Domain layer – pure business logic, no framework dependencies.
 *
 * <p>
 * Part of the hexagonal architecture core ({@code @Application}). Nothing in this package
 * may import adapter or infrastructure classes. ArchUnit's {@code ensureHexagonal()} rule
 * enforces this at build time.
 */
@Application
package com.fortytwotalents.claims.domain;

import org.jmolecules.architecture.hexagonal.Application;
