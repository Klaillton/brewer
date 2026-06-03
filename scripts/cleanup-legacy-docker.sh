#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$PROJECT_ROOT"

echo "Stopping legacy Docker Compose stacks for brewer..."

run_compose_down_if_exists() {
	local compose_file="$1"
	local args="$2"

	if [[ -f "$compose_file" ]]; then
		docker compose -f "$compose_file" down $args 2>/dev/null || true
	fi
}

run_compose_down_if_exists "legacy/docker-compose/docker-compose.yml" "--remove-orphans"
run_compose_down_if_exists "legacy/docker-compose/docker-compose.observability.yml" ""
run_compose_down_if_exists "legacy/docker-compose/docker-compose.sonarqube.yml" ""

echo "Legacy stacks stopped."
