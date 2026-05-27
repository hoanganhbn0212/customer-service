#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

if ! command -v docker >/dev/null 2>&1; then
  echo "Cần cài Docker trên server."
  exit 1
fi

if [[ ! -f .env ]]; then
  if [[ -f .env.example ]]; then
    cp .env.example .env
  fi
  if ! grep -q '^JWT_SECRET=.\+' .env 2>/dev/null; then
    secret=$(openssl rand -hex 32 2>/dev/null || echo "dev-only-change-me-$(date +%s)")
    if grep -q '^JWT_SECRET=' .env 2>/dev/null; then
      sed -i "s/^JWT_SECRET=.*/JWT_SECRET=$secret/" .env
    else
      echo "JWT_SECRET=$secret" >> .env
    fi
  fi
  if ! grep -q '^DB_PASSWORD=.\+' .env 2>/dev/null; then
    dbpass=$(openssl rand -hex 16 2>/dev/null || echo "dev-db-pass-$(date +%s)")
    sed -i "s/^DB_PASSWORD=.*/DB_PASSWORD=$dbpass/" .env || echo "DB_PASSWORD=$dbpass" >> .env
  fi
  echo "Đã tạo/sửa .env (chỉ cần JWT_SECRET + DB_PASSWORD cho chế độ HTTP)."
fi

set -a
# shellcheck disable=SC1091
source .env
set +a

if [[ -z "${JWT_SECRET:-}" || -z "${DB_PASSWORD:-}" ]]; then
  echo "Thiếu JWT_SECRET hoặc DB_PASSWORD trong .env"
  exit 1
fi

echo "=== Deploy thử (HTTP only, không domain) ==="
docker compose -f docker-compose.http.yml --env-file .env up -d --build

pub=""
if command -v curl >/dev/null 2>&1; then
  pub=$(curl -fsS --max-time 5 https://api.ipify.org 2>/dev/null || true)
fi

echo ""
echo "Xong. Mở trên trình duyệt / điện thoại (cùng mạng hoặc qua internet nếu mở port 80):"
if [[ -n "$pub" ]]; then
  echo "  http://${pub}"
else
  echo "  http://<IP-public-cua-VPS>"
fi
echo ""
echo "Lưu ý: không có HTTPS — chỉ để thử. Production nên dùng ./deploy.sh + domain thật."
