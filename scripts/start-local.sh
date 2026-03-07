#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$SCRIPT_DIR/.."

BACKEND_PID=""

cleanup() {
  echo ""
  echo "Shutting down..."
  [ -n "$BACKEND_PID" ] && kill "$BACKEND_PID" 2>/dev/null || true
  docker compose -f "$ROOT/docker-compose.yml" down
  echo "Done."
}
trap cleanup INT TERM EXIT

if lsof -ti :8080 >/dev/null 2>&1; then
  echo "Error: port 8080 is already in use. Stop the existing process first:"
  echo "  lsof -ti :8080 | xargs kill"
  exit 1
fi

echo "Starting PostgreSQL..."
docker compose -f "$ROOT/docker-compose.yml" up -d --wait

echo "Starting backend (Spring Boot)..."
"$ROOT/mvnw" -f "$ROOT/backend/pom.xml" -q spring-boot:run &
BACKEND_PID=$!

echo ""
echo "  App: http://localhost:8080/books"
echo ""
echo "Press Ctrl+C to stop all services."

wait "$BACKEND_PID"
