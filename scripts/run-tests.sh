#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$SCRIPT_DIR/.."

echo "=== Running backend tests (Testcontainers — no external DB needed) ==="
(cd "$ROOT" && ./mvnw -f backend/pom.xml -B verify)
echo "Backend tests: PASSED"
