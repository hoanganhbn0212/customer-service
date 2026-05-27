# Chay toan bo app bang Docker (Postgres + API + giao dien web)
$ErrorActionPreference = "Stop"
$root = Split-Path $PSScriptRoot -Parent
Set-Location $root

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Error "Chua cai Docker Desktop. Tai: https://www.docker.com/products/docker-desktop/"
    exit 1
}

if (-not (Test-Path ".env")) {
    Copy-Item ".env.example" ".env"
    Write-Host "Da tao file .env tu .env.example" -ForegroundColor Yellow
}

Write-Host "=== Docker: build + start (lan dau co the 5-15 phut) ===" -ForegroundColor Cyan
docker compose up -d --build

if ($LASTEXITCODE -ne 0) {
    Write-Error "docker compose that bai. Xem log: docker compose logs"
    exit 1
}

$port = "8080"
if (Test-Path ".env") {
    $line = Get-Content ".env" | Where-Object { $_ -match "^\s*APP_PORT\s*=" } | Select-Object -First 1
    if ($line -match "=\s*(\d+)") { $port = $Matches[1] }
}

$ip = & (Join-Path $PSScriptRoot "get-wifi-ip.ps1")

Write-Host ""
Write-Host "=== San sang ===" -ForegroundColor Green
Write-Host "  May tinh:     http://localhost:${port}/" -ForegroundColor Green
if ($ip) {
    Write-Host "  Dien thoai:   http://${ip}:${port}/  (cung Wi-Fi)" -ForegroundColor Green
}
Write-Host "  Dang nhap:    admin / password" -ForegroundColor DarkGray
Write-Host ""
Write-Host "Xem log:  docker compose logs -f" -ForegroundColor DarkGray
Write-Host "Dung app: docker compose down" -ForegroundColor DarkGray
