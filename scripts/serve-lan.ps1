# Build frontend + serve preview on LAN (phone opens http://<PC-IP>:4173)
$ErrorActionPreference = "Stop"
$root = Split-Path $PSScriptRoot -Parent
$frontend = Join-Path $root "frontend"
$healthUrl = "http://localhost:8082/api/health"

function Get-LanIpv4 {
    $addrs = Get-NetIPAddress -AddressFamily IPv4 -ErrorAction SilentlyContinue |
        Where-Object {
            $_.IPAddress -notlike "127.*" -and
            $_.IPAddress -notlike "169.254.*" -and
            $_.PrefixOrigin -ne "WellKnown"
        } |
        Select-Object -ExpandProperty IPAddress -Unique
    if ($addrs) { return $addrs[0] }
    return $null
}

function Test-BackendHealth {
    try {
        $r = Invoke-WebRequest -Uri $healthUrl -UseBasicParsing -TimeoutSec 2
        return $r.StatusCode -eq 200
    } catch {
        return $false
    }
}

if (-not (Get-Command npm -ErrorAction SilentlyContinue)) {
    Write-Error "Node/npm chua cai. Cai Node.js 18+ roi chay lai."
    exit 1
}

Write-Host "=== 1) Build frontend (production) ===" -ForegroundColor Cyan
Set-Location $frontend
if (-not (Test-Path "node_modules")) {
    npm install
}
npm run gen:api
npm run build

Write-Host ""
Write-Host "=== 2) Backend API ===" -ForegroundColor Cyan
if (-not (Test-BackendHealth)) {
    Write-Host "Backend chua chay. Mo terminal khac va chay:" -ForegroundColor Yellow
    Write-Host "  .\scripts\run-backend.ps1" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Hoac nhan Enter de mo cua so backend moi (can Docker Postgres da chay)." -ForegroundColor Yellow
    Read-Host "Nhan Enter khi san sang"
    $deadline = (Get-Date).AddMinutes(3)
    while (-not (Test-BackendHealth)) {
        if ((Get-Date) -gt $deadline) {
            Write-Error "Backend khong phan hoi tai $healthUrl. Kiem tra PostgreSQL (docker compose up -d) va chay lai."
            exit 1
        }
        Start-Sleep -Seconds 2
        Write-Host "  Cho backend..." -ForegroundColor DarkGray
    }
}
Write-Host "Backend OK: $healthUrl" -ForegroundColor Green

Write-Host ""
Write-Host "=== 3) Serve web (LAN) ===" -ForegroundColor Cyan
$ip = Get-LanIpv4
Write-Host ""
Write-Host "Mo tren DIEN THOAI (cung Wi-Fi):" -ForegroundColor Green
if ($ip) {
    Write-Host "  http://${ip}:4173" -ForegroundColor Green
} else {
    Write-Host "  http://<IP-may-tinh>:4173  (tim IP: ipconfig)" -ForegroundColor Yellow
}
Write-Host "Tren may tinh:" -ForegroundColor Green
Write-Host "  http://localhost:4173" -ForegroundColor Green
Write-Host ""
Write-Host "Dang nhap mac dinh: admin / password" -ForegroundColor DarkGray
Write-Host "Dung Ctrl+C de tat server preview." -ForegroundColor DarkGray
Write-Host ""

npm run preview:lan
