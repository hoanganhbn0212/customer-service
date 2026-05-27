# Chay thu tren DIEN THOAI — build 1 lan, preview nhanh hon dev (port 4173)
$ErrorActionPreference = "Stop"
$root = Split-Path $PSScriptRoot -Parent
$healthUrl = "http://localhost:8082/api/health"
$frontend = Join-Path $root "frontend"

function Test-BackendHealth {
    try {
        $r = Invoke-WebRequest -Uri $healthUrl -UseBasicParsing -TimeoutSec 3
        return $r.StatusCode -eq 200
    } catch { return $false }
}

$ip = & (Join-Path $PSScriptRoot "get-wifi-ip.ps1")

Set-Location $root
Write-Host "=== Che do dien thoai (build + preview) ===" -ForegroundColor Cyan

if (Get-Command docker -ErrorAction SilentlyContinue) {
    docker compose up -d postgres 2>&1 | Out-Null
}

if (-not (Test-BackendHealth)) {
    Write-Host "Khoi dong backend (cua so moi)..." -ForegroundColor Yellow
    Start-Process powershell -ArgumentList "-NoExit", "-ExecutionPolicy", "Bypass", "-File", (Join-Path $root "scripts\run-backend.ps1")
    $deadline = (Get-Date).AddMinutes(4)
    while (-not (Test-BackendHealth)) {
        if ((Get-Date) -gt $deadline) { Write-Error "Backend khong len." }
        Start-Sleep -Seconds 3
    }
}

# Tat frontend dev cu tren 5173 neu dang chay (tranh nham port)
$p5173 = Get-NetTCPConnection -LocalPort 5173 -State Listen -ErrorAction SilentlyContinue
if ($p5173) {
    Write-Host "Dang tat process cu tren 5173 (PID $($p5173.OwningProcess))..." -ForegroundColor Yellow
    Stop-Process -Id $p5173.OwningProcess -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 1
}

Set-Location $frontend
if (-not (Test-Path node_modules)) { npm install }
Write-Host "Build frontend (lan dau ~1-2 phut)..." -ForegroundColor Cyan
npm run gen:api 2>&1 | Out-Null
npm run build

Write-Host ""
Write-Host "Neu dien thoai van xoay lau: chay (Admin):" -ForegroundColor Yellow
Write-Host "  .\scripts\allow-lan-firewall.ps1" -ForegroundColor Yellow
Write-Host ""
Write-Host "=== Mo tren dien thoai (cung Wi-Fi) ===" -ForegroundColor Green
if ($ip) {
    Write-Host "  http://${ip}:4173/" -ForegroundColor Green
} else {
    Write-Host "  http://<IP-WiFi>:4173/  (ipconfig)" -ForegroundColor Yellow
}
Write-Host "  Dang nhap: admin / password" -ForegroundColor DarkGray
Write-Host ""

npm run preview:lan
