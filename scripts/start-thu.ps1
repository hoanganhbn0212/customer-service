# Chay thu tren Windows: Postgres (Docker) + Backend + Frontend — mot lenh
$ErrorActionPreference = "Stop"
$root = Split-Path $PSScriptRoot -Parent
$healthUrl = "http://localhost:8082/api/health"
$frontendUrl = "http://localhost:5173"

function Test-BackendHealth {
    try {
        $r = Invoke-WebRequest -Uri $healthUrl -UseBasicParsing -TimeoutSec 3
        return $r.StatusCode -eq 200
    } catch {
        return $false
    }
}

function Get-LanIpv4 {
    & (Join-Path $PSScriptRoot "get-wifi-ip.ps1")
}

Set-Location $root
Write-Host "=== Customer Service — che do chay thu ===" -ForegroundColor Cyan
Write-Host ""

# 1) PostgreSQL
Write-Host "[1/3] PostgreSQL (Docker)..." -ForegroundColor Cyan
if (Get-Command docker -ErrorAction SilentlyContinue) {
    docker compose up -d 2>&1 | Out-Host
    $ready = $false
    for ($i = 0; $i -lt 40; $i++) {
        $status = docker inspect --format "{{.State.Health.Status}}" customer-service-postgres 2>$null
        if ($status -eq "healthy") { $ready = $true; break }
        Start-Sleep -Seconds 2
    }
    if ($ready) {
        Write-Host "  Postgres san sang." -ForegroundColor Green
    } else {
        Write-Host "  Postgres chua healthy — van thu tiep (backend co the cho them)." -ForegroundColor Yellow
    }
} else {
    Write-Host "  Docker khong co — can Postgres tai localhost:5432 (user/pass trong application.yml)." -ForegroundColor Yellow
}

# 2) Backend
Write-Host "[2/3] Backend API (port 8082)..." -ForegroundColor Cyan
if (Test-BackendHealth) {
    Write-Host "  Backend da chay." -ForegroundColor Green
} else {
    $backendScript = Join-Path $root "scripts\run-backend.ps1"
    Write-Host "  Mo cua so PowerShell moi cho backend..." -ForegroundColor Yellow
    Start-Process powershell -ArgumentList "-NoExit", "-ExecutionPolicy", "Bypass", "-File", $backendScript
    $deadline = (Get-Date).AddMinutes(4)
    while (-not (Test-BackendHealth)) {
        if ((Get-Date) -gt $deadline) {
            Write-Error "Backend khong len sau 4 phut. Xem cua so backend (loi DB/port 8082)."
        }
        Start-Sleep -Seconds 3
        Write-Host "  Cho backend..." -ForegroundColor DarkGray
    }
    Write-Host "  Backend OK." -ForegroundColor Green
}

# 3) Frontend
Write-Host "[3/3] Frontend (port 5173)..." -ForegroundColor Cyan
if (-not (Get-Command npm -ErrorAction SilentlyContinue)) {
    Write-Error "Chua cai Node.js 18+. Tai: https://nodejs.org"
}

$frontend = Join-Path $root "frontend"
Set-Location $frontend
if (-not (Test-Path "node_modules")) {
    Write-Host "  npm install (lan dau)..." -ForegroundColor DarkGray
    npm install
}
Write-Host "  gen:api + dev server..." -ForegroundColor DarkGray
npm run gen:api | Out-Null

$ip = Get-LanIpv4
Write-Host ""
Write-Host "=== San sang ===" -ForegroundColor Green
Write-Host "  May tinh:     $frontendUrl" -ForegroundColor Green
Write-Host "  Admin users:  $frontendUrl/admin/users" -ForegroundColor Green
if ($ip) {
    Write-Host "  Dien thoai:   http://${ip}:5173  (cung Wi-Fi)" -ForegroundColor Green
}
Write-Host "  Dang nhap:    admin / password" -ForegroundColor DarkGray
Write-Host "  Dien thoai xoay lau? Chay: .\scripts\start-thu-device.ps1 (port 4173, nhanh hon)" -ForegroundColor Yellow
Write-Host "  Firewall:     .\scripts\allow-lan-firewall.ps1 (PowerShell Admin)" -ForegroundColor Yellow
Write-Host "  Ctrl+C de tat frontend. Backend van chay o cua so rieng." -ForegroundColor DarkGray
Write-Host ""

$env:VITE_DEVICE = "1"
npm run dev
