#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

if ! command -v docker >/dev/null 2>&1; then
  echo "Cần cài Docker + Docker Compose plugin trên server."
  exit 1
fi

if [[ ! -f .env ]]; then
  cp .env.example .env
  echo "Đã tạo deploy/.env — chỉnh DOMAIN, ACME_EMAIL, DB_PASSWORD, JWT_SECRET rồi chạy lại:"
  echo "  ./deploy.sh"
  exit 1
fi

set -a
# shellcheck disable=SC1091
source .env
set +a

if [[ -z "${JWT_SECRET:-}" ]]; then
  echo "JWT_SECRET trống. Sửa deploy/.env"
  exit 1
fi

if [[ -x ./preflight.sh ]]; then
  ./preflight.sh || exit 1
fi

echo "Deploying https://${DOMAIN} ..."
docker compose -f docker-compose.prod.yml --env-file .env up -d --build

echo ""
echo "Xong. Mở: https://${DOMAIN}"
echo "Đăng nhập mặc định: admin / password — đổi mật khẩu sau khi lên production."
echo "Logs: docker compose -f docker-compose.prod.yml logs -f"
