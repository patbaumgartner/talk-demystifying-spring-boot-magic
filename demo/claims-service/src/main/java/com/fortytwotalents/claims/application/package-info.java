/**
 * Application layer – orchestration of domain logic via ports.
 *
 * <p>
 * Part of the hexagonal architecture core ({@code @Application}). Classes here may depend
 * on the domain layer and on port interfaces, but never on adapter implementations or
 * infrastructure details.
 */
@Application
package com.fortytwotalents.claims.application;

import org.jmolecules.architecture.hexagonal.Application;
