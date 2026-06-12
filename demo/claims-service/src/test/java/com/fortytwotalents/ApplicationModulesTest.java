package com.fortytwotalents;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Architecture tests combining Spring Modulith module verification with jMolecules
 * hexagonal architecture annotations.
 *
 * <p>
 * Two layers of protection:
 * <ol>
 * <li><b>Runtime structure</b> ({@code modulesAreCompliant}): Spring Modulith reads the
 * package structure and verifies that no module references the internal types of another.
 * Package-private visibility + Modulith enforcement = no accidental cross-module
 * coupling.</li>
 * <li><b>Annotation documentation</b> (jMolecules): {@code @PrimaryPort},
 * {@code @SecondaryPort}, {@code @PrimaryAdapter}, {@code @SecondaryAdapter} and
 * {@code @Application} annotations express the hexagonal architecture intent directly on
 * the classes. IDE tooling, documentation generators and future ArchUnit integration
 * (once jmolecules-archunit ships a build against ArchUnit 1.x) can all read these
 * annotations.</li>
 * </ol>
 */
class ApplicationModulesTest {

	ApplicationModules modules = ApplicationModules.of(ClaimsApplication.class);

	/** Spring Modulith: no module references internal types of another module. */
	@Test
	void modulesAreCompliant() {
		modules.verify();
	}

	/** Generates PlantUML canvases under target/spring-modulith-docs/. */
	@Test
	void writeDocumentationSnippets() {
		new Documenter(modules).writeModuleCanvases();
	}

}
