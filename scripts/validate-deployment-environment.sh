#!/usr/bin/env bash
set -euo pipefail

if ! command -v docker >/dev/null 2>&1; then
  echo "[INFO] docker não encontrado neste ambiente. Pulando validação de containers legados."
  exit 0
fi

# Detecta containers do projeto brewer via labels do Docker Compose.
legacy_by_label="$(docker ps --filter label=com.docker.compose.project=brewer --format '{{.Names}}' || true)"

# Fallback para nomes conhecidos do stack legado.
legacy_by_name="$(docker ps --format '{{.Names}}' | grep -E '^(brewer-app|brewer-db|brewer-nginx)$' || true)"

if [[ -n "$legacy_by_label" || -n "$legacy_by_name" ]]; then
  echo "[ALERTA] Containers Docker Compose legados do projeto Brewer estão rodando."
  echo "[AÇÃO] Execute: ./scripts/cleanup-legacy-docker.sh"
  if [[ -n "$legacy_by_label" ]]; then
    echo "[DETALHE] Detectados por label do projeto brewer:"
    echo "$legacy_by_label"
  fi
  if [[ -n "$legacy_by_name" ]]; then
    echo "[DETALHE] Detectados por nome legado:"
    echo "$legacy_by_name"
  fi
  exit 2
fi

echo "Nenhum container Docker Compose legado do Brewer está rodando. Ambiente OK."
