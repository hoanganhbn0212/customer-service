#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

echo "=== Kiểm tra trước deploy ==="

if ! command -v docker >/dev/null 2>&1; then
  echo "[FAIL] Chưa cài Docker. Xem HUONG-DAN-VI.md bước 1."
  exit 1
fi
echo "[OK] Docker: $(docker --version)"

if ! docker compose version >/dev/null 2>&1; then
  echo "[FAIL] Chưa có Docker Compose plugin."
  exit 1
fi
echo "[OK] Compose: $(docker compose version)"

for port in 80 443; do
  if ss -tln 2>/dev/null | grep -q ":${port} " || netstat -tln 2>/dev/null | grep -q ":${port} "; then
    echo "[WARN] Cổng ${port} đang được dùng — deploy có thể conflict (nginx/apache khác?)"
  else
    echo "[OK] Cổng ${port} trống"
  fi
done

if [[ ! -f .env ]]; then
  echo "[WARN] Chưa có deploy/.env — chạy: cp .env.example .env && nano .env"
  exit 1
fi

set -a
# shellcheck disable=SC1091
source .env
set +a

if [[ -z "${DOMAIN:-}" ]]; then
  echo "[FAIL] DOMAIN trống trong .env"
  exit 1
fi
if [[ -z "${JWT_SECRET:-}" ]]; then
  echo "[FAIL] JWT_SECRET trống trong .env"
  exit 1
fi
if [[ -z "${DB_PASSWORD:-}" ]]; then
  echo "[FAIL] DB_PASSWORD trống trong .env"
  exit 1
fi
echo "[OK] File .env có DOMAIN, JWT_SECRET, DB_PASSWORD"

if command -v dig >/dev/null 2>&1; then
  resolved=$(dig +short "$DOMAIN" A | head -1)
  if [[ -z "$resolved" ]]; then
    echo "[WARN] DNS: chưa thấy bản ghi A cho $DOMAIN"
  else
    echo "[OK] DNS $DOMAIN → $resolved"
    if command -v curl >/dev/null 2>&1; then
      pub=$(curl -fsS --max-time 5 https://api.ipify.org 2>/dev/null || curl -fsS --max-time 5 ifconfig.me 2>/dev/null || true)
      if [[ -n "$pub" && "$resolved" != "$pub" ]]; then
        echo "[WARN] IP public VPS ($pub) khác IP DNS ($resolved) — HTTPS có thể fail"
      fi
    fi
  fi
else
  echo "[SKIP] Không có lệnh dig — bỏ qua kiểm tra DNS"
fi

echo ""
echo "Sẵn sàng deploy: ./deploy.sh"
