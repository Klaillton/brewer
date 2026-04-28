#!/usr/bin/env bash
set -euo pipefail

NAMESPACE="brewer"
SECRET_NAME="brewer-secrets"
ENV_FILE="k8s/.secret.env"
GENERATE="false"

usage() {
  cat <<'EOF'
Uso:
  scripts/rotate-k8s-secret.sh
    Aplica/rotaciona o secret usando k8s/.secret.env

  scripts/rotate-k8s-secret.sh --generate
    Gera novas credenciais fortes em k8s/.secret.env e aplica no cluster

  scripts/rotate-k8s-secret.sh --file caminho.env
    Usa arquivo .env alternativo
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --generate)
      GENERATE="true"
      shift
      ;;
    --file)
      ENV_FILE="$2"
      shift 2
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Argumento inválido: $1" >&2
      usage
      exit 1
      ;;
  esac
done

if [[ "$GENERATE" == "true" ]]; then
  # Captura senha root ATUAL do cluster antes de gerar novas (necessário para ALTER USER)
  OLD_ROOT_PASSWORD=""
  if kubectl get secret "$SECRET_NAME" -n "$NAMESPACE" &>/dev/null; then
    OLD_ROOT_PASSWORD="$(kubectl get secret "$SECRET_NAME" -n "$NAMESPACE" \
      -o jsonpath='{.data.db-root-password}' | base64 -d)"
  fi

  mkdir -p "$(dirname "$ENV_FILE")"
  DB_ROOT_PASSWORD="$(openssl rand -base64 36 | tr -d '\n' | tr '+/' 'Aa' | cut -c1-40)"
  DB_PASSWORD="$(openssl rand -base64 36 | tr -d '\n' | tr '+/' 'Bb' | cut -c1-40)"
  cat > "$ENV_FILE" <<EOF
DB_ROOT_PASSWORD=$DB_ROOT_PASSWORD
DB_PASSWORD=$DB_PASSWORD
DB_USER=brewer
DB_NAME=brewer
EOF
  chmod 600 "$ENV_FILE"
  echo "[OK] Arquivo gerado: $ENV_FILE"
fi

if [[ ! -f "$ENV_FILE" ]]; then
  echo "[ERRO] Arquivo não encontrado: $ENV_FILE" >&2
  echo "Crie com base em k8s/.secret.env.example" >&2
  exit 1
fi

set -a
# shellcheck disable=SC1090
source "$ENV_FILE"
set +a

: "${DB_ROOT_PASSWORD:?DB_ROOT_PASSWORD não definido}"
: "${DB_PASSWORD:?DB_PASSWORD não definido}"
: "${DB_USER:?DB_USER não definido}"
: "${DB_NAME:?DB_NAME não definido}"

kubectl create namespace "$NAMESPACE" --dry-run=client -o yaml | kubectl apply -f - >/dev/null

kubectl create secret generic "$SECRET_NAME" -n "$NAMESPACE" \
  --from-literal=db-root-password="$DB_ROOT_PASSWORD" \
  --from-literal=db-password="$DB_PASSWORD" \
  --from-literal=db-user="$DB_USER" \
  --from-literal=db-name="$DB_NAME" \
  --dry-run=client -o yaml | kubectl apply -f -

echo "[OK] Secret $SECRET_NAME aplicado no namespace $NAMESPACE"

# Se foi geração de novas credenciais, atualiza as senhas no MariaDB ANTES de reiniciar
if [[ "${GENERATE:-false}" == "true" ]] && [[ -n "${OLD_ROOT_PASSWORD:-}" ]]; then
  echo "[INFO] Atualizando senhas no MariaDB..."
  MARIADB_POD="$(kubectl get pod -n "$NAMESPACE" -l app=mariadb \
    -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || true)"

  if [[ -z "$MARIADB_POD" ]]; then
    echo "[AVISO] Pod MariaDB não encontrado — senhas serão ativas após o próximo start do MariaDB" >&2
  else
    kubectl exec -n "$NAMESPACE" "$MARIADB_POD" -- \
      mariadb -u root -p"${OLD_ROOT_PASSWORD}" -e \
      "ALTER USER 'root'@'localhost' IDENTIFIED BY '${DB_ROOT_PASSWORD}';
       ALTER USER '${DB_USER}'@'%' IDENTIFIED BY '${DB_PASSWORD}';
       FLUSH PRIVILEGES;" \
    && echo "[OK] Senhas atualizadas no MariaDB" \
    || { echo "[ERRO] Falha ao atualizar senhas no MariaDB. Verifique manualmente." >&2; exit 1; }
  fi
fi

echo "[INFO] Reiniciando deployments que usam o secret..."
kubectl rollout restart deployment/mariadb -n "$NAMESPACE"
kubectl rollout restart deployment/brewer -n "$NAMESPACE"
kubectl rollout status deployment/mariadb -n "$NAMESPACE" --timeout=180s
kubectl rollout status deployment/brewer -n "$NAMESPACE" --timeout=600s

echo "[OK] Rotação concluída com sucesso"
