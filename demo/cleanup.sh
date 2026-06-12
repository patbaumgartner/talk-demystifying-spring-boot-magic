#!/usr/bin/env bash
# cleanup-projects.sh
# Runs Maven cleanup/upgrade steps for every project (depth-1 subdirectory
# containing a pom.xml) found relative to this script.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Regex that shellcheck-safe: passed via -D so quoting the dot is fine in mvn args
IGNORE_VERSIONS=".*[-_.](alpha|Alpha|ALPHA|b|beta|Beta|BETA|rc|RC|M|EA)[-_.]?[0-9]*"

run_cleanup() {
    local project_dir="$1"
    echo "========================================================"
    echo "  Cleaning up: ${project_dir}"
    echo "========================================================"
    cd "${project_dir}"

    mvn org.codehaus.mojo:versions-maven-plugin:update-parent \
        -DallowSnapshots=false \
        -DgenerateBackupPoms=false \
        "-Dmaven.version.ignore=${IGNORE_VERSIONS}"

    mvn org.codehaus.mojo:versions-maven-plugin:update-properties \
        -DallowSnapshots=false \
        -DallowMajorUpdates=true \
        -DallowMinorUpdates=true \
        -DallowIncrementalUpdates=true \
        "-Dmaven.version.ignore=${IGNORE_VERSIONS}"

    mvn tidy:pom

    mvn -U com.github.ekryd.sortpom:sortpom-maven-plugin:sort \
        -Dsort.predefinedSortOrder=custom_1

    mvn -Dmaven.gitcommitid.skip=true \
        -Dcyclonedx.skip=true \
        org.openrewrite.maven:rewrite-maven-plugin:run \
        -Drewrite.activeRecipes=org.openrewrite.staticanalysis.CodeCleanup,org.openrewrite.java.logging.slf4j.Slf4jBestPractices,org.openrewrite.java.testing.junit.JUnit6BestPractices,org.openrewrite.java.testing.mockito.MockitoBestPractices,org.openrewrite.java.spring.boot4.UpgradeSpringBoot_4_0 \
        -Drewrite.recipeArtifactCoordinates=io.moderne.recipe:rewrite-spring:LATEST,org.openrewrite.recipe:rewrite-static-analysis:LATEST,org.openrewrite.recipe:rewrite-logging-frameworks:LATEST,org.openrewrite.recipe:rewrite-testing-frameworks:LATEST \
        '-Drewrite.exclusions=**/PetTypeFormatterTests.java'

    mvn -Dmaven.gitcommitid.skip=true \
        -Dcyclonedx.skip=true \
        org.openrewrite.maven:rewrite-maven-plugin:run \
        -Drewrite.activeRecipes=org.openrewrite.java.RemoveUnusedImports,org.openrewrite.java.OrderImports \
        -Drewrite.exportDatatables=true

    mvn -U io.spring.javaformat:spring-javaformat-maven-plugin:apply

    rm -f pom.xml.versionsBackup pom.xml.bak

    cd "${SCRIPT_DIR}"
}

main() {
    local failed=()

    while IFS= read -r -d '' pom; do
        project_dir="$(dirname "${pom}")"
        if run_cleanup "${project_dir}"; then
            echo "  OK: ${project_dir}"
        else
            echo "  FAILED: ${project_dir}" >&2
            failed+=("${project_dir}")
        fi
    done < <(find "${SCRIPT_DIR}" -mindepth 2 -maxdepth 2 -name "pom.xml" -print0 | sort -z)

    if [[ ${#failed[@]} -gt 0 ]]; then
        echo "" >&2
        echo "The following projects failed:" >&2
        for p in "${failed[@]}"; do
            echo "  - ${p}" >&2
        done
        exit 1
    fi

    echo ""
    echo "All projects cleaned up successfully."
}

main
