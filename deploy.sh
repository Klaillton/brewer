#!/usr/bin/env bash
# ──────────────────────────────────────────────────────────────────────────────
# deploy.sh — Build e deploy do Brewer no K3s local
# ──────────────────────────────────────────────────────────────────────────────
# Uso:
#   ./deploy.sh             # build da imagem + deploy completo
#   ./deploy.sh --skip-build  # apenas aplica os manifests (imagem já existe)
#   ./deploy.sh --delete      # remove todos os recursos do namespace brewer
#
# Pré-requisitos:
#   - docker instalado e com acesso ao daemon
#   - kubectl configurado apontando para o cluster K3s
#   - K3s usando docker como container runtime (docker://29.4.x)
#   - namespace observability com otel-collector acessível em
#     otel-collector.observability.svc.cluster.local:4318
# ──────────────────────────────────────────────────────────────────────────────
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR"
K8S_DIR="$PROJECT_ROOT/k8s"

IMAGE_NAME="brewer"
IMAGE_TAG="latest"
NAMESPACE="brewer"
SECRET_SCRIPT="$PROJECT_ROOT/scripts/rotate-k8s-secret.sh"
SECRET_ENV_FILE="$PROJECT_ROOT/k8s/.secret.env"

# ── Funções de log ────────────────────────────────────────────────────────────
info()    { echo "[INFO]  $*"; }
success() { echo "[OK]    $*"; }
warn()    { echo "[WARN]  $*"; }
error()   { echo "[ERROR] $*" >&2; exit 1; }

# ── Argumento --delete ────────────────────────────────────────────────────────
if [[ "${1:-}" == "--delete" ]]; then
  warn "Removendo todos os recursos do namespace $NAMESPACE ..."
  kubectl delete namespace "$NAMESPACE" --ignore-not-found
  success "Namespace $NAMESPACE removido."
  exit 0
fi

# ── Verificação de dependências ───────────────────────────────────────────────
command -v docker  >/dev/null 2>&1 || error "docker não encontrado. Instale o Docker."
command -v kubectl >/dev/null 2>&1 || error "kubectl não encontrado."

# ── Secret (fora do repositório) ─────────────────────────────────────────────
if [[ -f "$SECRET_ENV_FILE" ]]; then
  info "Aplicando secret a partir de $SECRET_ENV_FILE ..."
  "$SECRET_SCRIPT" --file "$SECRET_ENV_FILE"
elif kubectl get secret brewer-secrets -n "$NAMESPACE" >/dev/null 2>&1; then
  info "Secret brewer-secrets já existe no cluster."
else
  error "Secret brewer-secrets não encontrado. Crie k8s/.secret.env (base: k8s/.secret.env.example) e rode scripts/rotate-k8s-secret.sh"
fi

# ── Build da imagem Docker ────────────────────────────────────────────────────
if [[ "${1:-}" != "--skip-build" ]]; then
  info "Construindo imagem $IMAGE_NAME:$IMAGE_TAG ..."
  docker build -t "$IMAGE_NAME:$IMAGE_TAG" "$PROJECT_ROOT"
  success "Imagem construída: $IMAGE_NAME:$IMAGE_TAG"

  # K3s usa docker como runtime neste servidor, então a imagem já está disponível
  # no namespace do containerd via Docker socket compartilhado.
  # Se o runtime fosse containerd puro, seria necessário:
  #   docker save "$IMAGE_NAME:$IMAGE_TAG" | sudo k3s ctr images import -
  info "Imagem disponível para K3s (runtime: docker)."
fi

# ── Aplicar manifests ─────────────────────────────────────────────────────────
info "Aplicando manifests Kubernetes em $K8S_DIR ..."
kubectl apply -k "$K8S_DIR"
success "Manifests aplicados."

# ── Aguardar MariaDB ficar pronto ─────────────────────────────────────────────
info "Aguardando MariaDB ficar pronto ..."
kubectl rollout status deployment/mariadb -n "$NAMESPACE" --timeout=180s
success "MariaDB pronto."

# ── Aguardar aplicação ficar pronta ──────────────────────────────────────────
info "Aguardando aplicação brewer ficar pronta ..."
kubectl rollout status deployment/brewer -n "$NAMESPACE" --timeout=300s
success "Aplicação brewer pronta."

# ── Resumo ────────────────────────────────────────────────────────────────────
echo ""
echo "══════════════════════════════════════════════════════════"
echo "  Deploy concluído com sucesso!"
echo "══════════════════════════════════════════════════════════"
echo ""
kubectl get all -n "$NAMESPACE"
echo ""
echo "  Acesso à aplicação:"
echo "    • Via Ingress (host): http://brewer.local"
echo "      (adicione '192.168.1.101 brewer.local' ao /etc/hosts)"
echo ""
echo "  Observabilidade (acesso via port-forward):"
echo "    kubectl port-forward svc/grafana    3000:3000 -n observability"
echo "    kubectl port-forward svc/prometheus 9090:9090 -n observability"
echo ""
