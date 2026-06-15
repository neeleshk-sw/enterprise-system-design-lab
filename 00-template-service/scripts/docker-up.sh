#!/usr/bin/env bash
# Build images and bring up the full stack (Postgres + customer-service).
set -euo pipefail

cd "$(dirname "$0")/.."
exec docker compose up --build "$@"
