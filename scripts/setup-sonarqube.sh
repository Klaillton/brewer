#!/usr/bin/env bash
# =============================================================================
# setup-sonarqube.sh
#
# Bootstrap do servidor SonarQube local:
#   - Aguarda o servidor ficar disponível
#   - Troca a senha padrão (admin/admin → nova senha)
#   - Cria um token de análise global (salvo em ~/.sonar/token)
#   - Gera/atualiza ~/.sonar/sonar-scanner.properties para uso em todos
#     os projetos futuros (padronização do servidor local)
#
# Uso:
#   ./scripts/setup-sonarqube.sh
#
# Variáveis de ambiente opcionais:
#   SONAR_URL        URL do servidor  (padrão: http://localhost:9000)
#   SONAR_NEW_PASS   Nova senha admin (padrão: solicita interativamente)
#   SONAR_TOKEN_NAME Nome do token    (padrão: local-dev)
# =============================================================================
set -euo pipefail

SONAR_URL="${SONAR_URL:-http://localhost:9000}"
TOKEN_NAME="${SONAR_TOKEN_NAME:-local-dev}"
TOKEN_FILE="$HOME/.sonar/token"
SCANNER_PROPS="$HOME/.sonar/sonar-scanner.properties"

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; NC='\033[0m'
info()  { echo -e "${GREEN}[INFO]${NC}  $*"; }
warn()  { echo -e "${YELLOW}[WARN]${NC}  $*"; }
error() { echo -e "${RED}[ERROR]${NC} $*" >&2; }

# ---------------------------------------------------------------------------
# 1. Verifica dependências
# ---------------------------------------------------------------------------
for cmd in curl jq; do
  if ! command -v "$cmd" &>/dev/null; then
    error "Comando '$cmd' não encontrado. Instale-o e tente novamente."
    exit 1
  fi
done

# ---------------------------------------------------------------------------
# 2. Aguarda SonarQube ficar UP (máx. 5 min)
# ---------------------------------------------------------------------------
info "Aguardando SonarQube em $SONAR_URL ..."
TIMEOUT=300
ELAPSED=0
until curl -sf "$SONAR_URL/api/system/status" 2>/dev/null | grep -q '"status":"UP"'; do
  if [[ $ELAPSED -ge $TIMEOUT ]]; then
    error "SonarQube não ficou disponível em ${TIMEOUT}s. Verifique: docker compose -f docker-compose.sonarqube.yml logs sonarqube"
    exit 1
  fi
  sleep 5
  ELAPSED=$((ELAPSED + 5))
  echo -n "."
done
echo ""
info "SonarQube está UP."

# ---------------------------------------------------------------------------
# 3. Troca de senha padrão
# ---------------------------------------------------------------------------
if [[ -z "${SONAR_NEW_PASS:-}" ]]; then
  echo -n "Digite a nova senha para o admin (mín. 12 caracteres): "
  read -rs SONAR_NEW_PASS
  echo ""
fi

if [[ ${#SONAR_NEW_PASS} -lt 12 ]]; then
  error "A senha deve ter pelo menos 12 caracteres."
  exit 1
fi

HTTP_STATUS=$(curl -sf -o /dev/null -w "%{http_code}" \
  -u admin:admin \
  -X POST "$SONAR_URL/api/users/change_password" \
  -d "login=admin&previousPassword=admin&password=$SONAR_NEW_PASS" 2>/dev/null || echo "000")

if [[ "$HTTP_STATUS" == "204" ]]; then
  info "Senha do admin alterada com sucesso."
elif [[ "$HTTP_STATUS" == "401" ]]; then
  warn "Senha padrão já foi alterada. Continuando com a senha fornecida..."
else
  warn "Troca de senha retornou HTTP $HTTP_STATUS. Pode ser que já esteja configurada."
fi

ADMIN_PASS="$SONAR_NEW_PASS"

# ---------------------------------------------------------------------------
# 4. Cria token de análise
# ---------------------------------------------------------------------------
mkdir -p "$HOME/.sonar"

# Remove token anterior de mesmo nome para evitar conflito
curl -sf -u "admin:$ADMIN_PASS" \
  -X POST "$SONAR_URL/api/user_tokens/revoke" \
  -d "name=$TOKEN_NAME" &>/dev/null || true

TOKEN_RESPONSE=$(curl -sf -u "admin:$ADMIN_PASS" \
  -X POST "$SONAR_URL/api/user_tokens/generate" \
  -d "name=$TOKEN_NAME&type=GLOBAL_ANALYSIS_TOKEN")

TOKEN=$(echo "$TOKEN_RESPONSE" | jq -r '.token')

if [[ -z "$TOKEN" || "$TOKEN" == "null" ]]; then
  error "Falha ao gerar token. Resposta: $TOKEN_RESPONSE"
  exit 1
fi

echo "$TOKEN" > "$TOKEN_FILE"
chmod 600 "$TOKEN_FILE"
info "Token salvo em: $TOKEN_FILE"

# ---------------------------------------------------------------------------
# 5. Gera ~/.sonar/sonar-scanner.properties (padronização global)
# ---------------------------------------------------------------------------
cat > "$SCANNER_PROPS" <<EOF
# SonarScanner — configuração global para projetos locais
# Gerado por scripts/setup-sonarqube.sh em $(date '+%Y-%m-%d %H:%M:%S')
#
# Para usar em qualquer projeto, basta executar:
#   mvn sonar:sonar -Dsonar.token=\$(cat ~/.sonar/token)
# Ou defina a variável SONAR_TOKEN:
#   export SONAR_TOKEN=\$(cat ~/.sonar/token)

sonar.host.url=$SONAR_URL
sonar.token=$TOKEN
EOF

chmod 600 "$SCANNER_PROPS"
info "Configuração global salva em: $SCANNER_PROPS"

# ---------------------------------------------------------------------------
# 6. Resumo
# ---------------------------------------------------------------------------
echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  SonarQube local configurado!${NC}"
echo -e "${GREEN}========================================${NC}"
echo "  URL:    $SONAR_URL"
echo "  Token:  $(cat "$TOKEN_FILE" | cut -c1-8)... (salvo em $TOKEN_FILE)"
echo ""
echo "  Para analisar este projeto:"
echo "    ./mvnw sonar:sonar -Dsonar.token=\$(cat ~/.sonar/token)"
echo ""
echo "  Para projetos futuros (token já em ~/.sonar/sonar-scanner.properties):"
echo "    ./mvnw sonar:sonar"
echo ""
