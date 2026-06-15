#!/usr/bin/env bash
# Build and test the whole module. Pins JAVA_HOME to a JDK 21 on macOS if unset.
set -euo pipefail

if [ -z "${JAVA_HOME:-}" ] && command -v /usr/libexec/java_home >/dev/null 2>&1; then
  export JAVA_HOME="$(/usr/libexec/java_home -v 21 2>/dev/null || true)"
fi

cd "$(dirname "$0")/.."
exec mvn clean verify "$@"
