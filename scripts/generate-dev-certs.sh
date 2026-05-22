#!/usr/bin/env bash
# ─── Geração de certificado autoassinado para desenvolvimento ─────────────────
# Gera cert.pem + key.pem em nginx/certs/ para uso local com o Nginx.
# NÃO use em produção; substitua por certificados reais ou cert-manager.
# ─────────────────────────────────────────────────────────────────────────────
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CERTS_DIR="$SCRIPT_DIR/../nginx/certs"

mkdir -p "$CERTS_DIR"

openssl req -x509 -nodes -days 365 \
  -newkey rsa:2048 \
  -keyout "$CERTS_DIR/key.pem" \
  -out    "$CERTS_DIR/cert.pem" \
  -subj   "/CN=localhost/O=Brewer Dev/C=BR" \
  -addext "subjectAltName=DNS:localhost,DNS:brewer.local,IP:127.0.0.1"

chmod 600 "$CERTS_DIR/key.pem"
chmod 644 "$CERTS_DIR/cert.pem"

echo "Certificado autoassinado gerado em $CERTS_DIR"
echo "  cert.pem  — certificado público"
echo "  key.pem   — chave privada (não versionar)"
