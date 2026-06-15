#!/usr/bin/env bash
# Start PostgreSQL in Docker, then run the service on the host (port 8080).
set -euo pipefail

cd "$(dirname "$0")/.."
docker compose up -d postgres

if [ -z "${JAVA_HOME:-}" ] && command -v /usr/libexec/java_home >/dev/null 2>&1; then
  export JAVA_HOME="$(/usr/libexec/java_home -v 21 2>/dev/null || true)"
fi

exec mvn -pl customer-service -am spring-boot:run
