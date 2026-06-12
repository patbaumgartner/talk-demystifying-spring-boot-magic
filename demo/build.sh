#!/usr/bin/env bash
# build.sh
# Builds and installs the audit-spring-boot starter, then builds the claims-service.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

STARTER_DIR="${SCRIPT_DIR}/audit-spring-boot"
APP_DIR="${SCRIPT_DIR}/claims-service"

MVN="${STARTER_DIR}/mvnw"

echo "========================================================"
echo "  Step 1: Build & install audit-spring-boot starter"
echo "========================================================"
cd "${STARTER_DIR}"
"${MVN}" install

echo ""
echo "========================================================"
echo "  Step 2: Build claims-service"
echo "========================================================"
cd "${APP_DIR}"
"${MVN}" verify

echo ""
echo "Build successful."
