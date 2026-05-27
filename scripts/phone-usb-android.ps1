# Android + cap USB: mo http://localhost:8080 tren dien thoai (khong can Wi-Fi)
# Yeu cau: Docker dang chay (.\scripts\docker-up.ps1), USB debugging bat, adb co trong PATH

$ErrorActionPreference = "Stop"
$port = 8080
if (Test-Path (Join-Path (Split-Path $PSScriptRoot -Parent) ".env")) {
    $line = Get-Content (Join-Path (Split-Path $PSScriptRoot -Parent) ".env") |
        Where-Object { $_ -match "^\s*APP_PORT\s*=" } | Select-Object -First 1
    if ($line -match "=\s*(\d+)") { $port = [int]$Matches[1] }
}

if (-not (Get-Command adb -ErrorAction SilentlyContinue)) {
    Write-Host "Chua co adb (Android Platform Tools)." -ForegroundColor Yellow
    Write-Host "Tai: https://developer.android.com/tools/releases/platform-tools" -ForegroundColor Yellow
    Write-Host "Hoac cai Android Studio -> SDK Platform-Tools -> them vao PATH." -ForegroundColor Yellow
    exit 1
}

$devices = adb devices | Select-String "device$"
if (-not $devices) {
    Write-Host "Khong thay dien thoai." -ForegroundColor Red
    Write-Host "1. Cap USB + cho phep USB debugging tren may" -ForegroundColor Yellow
    Write-Host "2. Chay: adb devices" -ForegroundColor Yellow
    exit 1
}

try {
    $null = Invoke-WebRequest "http://localhost:${port}/" -UseBasicParsing -TimeoutSec 3
} catch {
    Write-Host "PC chua co app tren port ${port}. Chay truoc:" -ForegroundColor Red
    Write-Host "  cd C:\Users\ADMIN\AnhDH\customer-service" -ForegroundColor Yellow
    Write-Host "  .\scripts\docker-up.ps1" -ForegroundColor Yellow
    exit 1
}

adb reverse tcp:${port} tcp:${port}
Write-Host ""
Write-Host "=== Tren dien thoai Android (Chrome) ===" -ForegroundColor Green
Write-Host "  http://localhost:${port}/" -ForegroundColor Green
Write-Host "  Dang nhap: admin / password" -ForegroundColor DarkGray
Write-Host ""
Write-Host "Rut cap hoac tat reverse: adb reverse --remove tcp:${port}" -ForegroundColor DarkGray
