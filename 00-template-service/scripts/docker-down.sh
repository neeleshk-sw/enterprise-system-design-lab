#!/usr/bin/env bash
# Stop the stack. Pass -v to also remove the Postgres data volume.
set -euo pipefail

cd "$(dirname "$0")/.."
exec docker compose down "$@"
