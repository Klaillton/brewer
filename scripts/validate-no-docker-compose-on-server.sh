#!/usr/bin/env bash
set -euo pipefail

if [[ "${ALLOW_DOCKER_COMPOSE_ON_SERVER:-false}" == "true" ]]; then
  echo "[WARN] Guardrail desativado por ALLOW_DOCKER_COMPOSE_ON_SERVER=true"
  exit 0
fi

# Bloqueia uso de Docker Compose no servidor devserverpi
if hostname | grep -qi devserverpi; then
  echo "[ERRO] O uso de Docker Compose é proibido neste servidor (devserverpi). Utilize apenas Kubernetes (K3s)."
  exit 1
fi

echo "Ambiente validado: Docker Compose permitido apenas em ambiente local."
