#!/usr/bin/env bash
# ─── Instala a CA local do brewer no trust store do sistema ───────────────────
# Após rodar este script, browsers baseados em NSS (Chrome, Chromium, Firefox)
# e ferramentas de linha de comando (curl, wget) confiarão automaticamente no
# certificado de https://brewer.local.
#
# Requer: kubectl, openssl, sudo
# ─────────────────────────────────────────────────────────────────────────────
set -euo pipefail

CA_CERT="/tmp/brewer-local-ca.crt"
CA_NAME="brewer-local-ca"

echo "==> Aguardando o certificado da CA ser emitido pelo cert-manager..."
kubectl wait certificate/brewer-local-ca \
  --namespace cert-manager \
  --for=condition=ready \
  --timeout=60s

echo "==> Exportando certificado da CA..."
kubectl get secret brewer-local-ca-tls \
  --namespace cert-manager \
  -o jsonpath='{.data.tls\.crt}' | base64 -d > "$CA_CERT"

echo "==> Informações do certificado:"
openssl x509 -in "$CA_CERT" -noout -subject -issuer -dates

# ─── Instalação no trust store do sistema (Linux) ────────────────────────────
if command -v update-ca-certificates &>/dev/null || [ -x /usr/sbin/update-ca-certificates ]; then
  UPDATE_CA_CERTS="${UPDATE_CA_CERTS:-/usr/sbin/update-ca-certificates}"
  # Debian / Ubuntu / Raspbian
  sudo cp "$CA_CERT" "/usr/local/share/ca-certificates/${CA_NAME}.crt"
  sudo "$UPDATE_CA_CERTS"
  echo "==> CA instalada via update-ca-certificates."
elif command -v update-ca-trust &>/dev/null || [ -x /usr/sbin/update-ca-trust ]; then
  # RHEL / CentOS / Fedora
  sudo cp "$CA_CERT" "/etc/pki/ca-trust/source/anchors/${CA_NAME}.crt"
  sudo /usr/sbin/update-ca-trust extract
  echo "==> CA instalada via update-ca-trust."
else
  echo "AVISO: gerenciador de CA não reconhecido. Instale manualmente:"
  echo "  $CA_CERT"
fi

# ─── Instalação no NSS DB (Chrome / Chromium / Edge no Linux) ────────────────
CERTUTIL_BIN=$(command -v certutil 2>/dev/null || echo /usr/bin/certutil)
if [ -x "$CERTUTIL_BIN" ]; then
  for NSS_DB in \
    "$HOME/.pki/nssdb" \
    "$HOME/snap/chromium/current/.pki/nssdb" \
    "$HOME/.mozilla/firefox/"*.default-release; do
    if [ -d "$NSS_DB" ]; then
      "$CERTUTIL_BIN" -d "sql:$NSS_DB" -A -n "$CA_NAME" -t "C,," -i "$CA_CERT" 2>/dev/null && \
        echo "==> CA adicionada ao NSS DB: $NSS_DB" || true
    fi
  done
else
  echo "AVISO: 'certutil' não encontrado. Para instalar no Chrome/Chromium:"
  echo "  sudo apt-get install -y libnss3-tools"
  echo "  certutil -d sql:\$HOME/.pki/nssdb -A -n brewer-local-ca -t 'C,,' -i $CA_CERT"
fi

rm -f "$CA_CERT"
echo ""
echo "Concluído. Reinicie o browser para que as mudanças tenham efeito."
