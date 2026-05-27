# Build APK Android + cai qua USB (adb)
$ErrorActionPreference = "Stop"
$root = Split-Path $PSScriptRoot -Parent
$frontend = Join-Path $root "frontend"
$envFile = Join-Path $frontend ".env.capacitor.local"
$envExample = Join-Path $frontend ".env.capacitor.example"

Set-Location $frontend

if (-not (Test-Path $envFile)) {
    Copy-Item $envExample $envFile
    $ip = & (Join-Path $PSScriptRoot "get-wifi-ip.ps1")
    if ($ip) {
        (Get-Content $envFile) -replace "192.168.1.10", $ip | Set-Content $envFile
        Write-Host "Da tao .env.capacitor.local voi IP Wi-Fi: $ip" -ForegroundColor Yellow
    } else {
        Write-Host "Sua file frontend\.env.capacitor.local — VITE_API_BASE_URL=http://<IP-PC>:8080" -ForegroundColor Yellow
    }
    Write-Host "Can Docker dang chay: .\scripts\docker-up.ps1" -ForegroundColor Yellow
    Read-Host "Sua xong IP thi nhan Enter"
}

# Load env for Vite build
Get-Content $envFile | ForEach-Object {
    if ($_ -match "^\s*VITE_API_BASE_URL\s*=\s*(.+)$") {
        $env:VITE_API_BASE_URL = $Matches[1].Trim()
    }
}
if (-not $env:VITE_API_BASE_URL) {
    Write-Error "Thieu VITE_API_BASE_URL trong .env.capacitor.local"
}

Write-Host "API URL cho app: $env:VITE_API_BASE_URL" -ForegroundColor Cyan

if (-not (Test-Path "node_modules")) { npm install }

Write-Host "=== 1/4 Build Vue (Capacitor) ===" -ForegroundColor Cyan
npm run build:cap
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

if (-not (Test-Path "android")) {
    Write-Host "=== Them project Android (lan dau) ===" -ForegroundColor Cyan
    npx cap add android
}

Write-Host "=== 2/4 Cap sync ===" -ForegroundColor Cyan
npx cap sync android

# Cho phep HTTP (API local Docker)
$manifest = Join-Path $frontend "android\app\src\main\AndroidManifest.xml"
if (Test-Path $manifest) {
    $xml = Get-Content $manifest -Raw
    if ($xml -notmatch "usesCleartextTraffic") {
        $xml = $xml -replace "<application", '<application android:usesCleartextTraffic="true"'
        Set-Content $manifest $xml -NoNewline
        Write-Host "Da bat usesCleartextTraffic (HTTP API local)" -ForegroundColor DarkGray
    }
}

Write-Host "=== 3/4 Build APK ===" -ForegroundColor Cyan
Push-Location android
try {
    if (-not (Test-Path "gradlew.bat")) { throw "Thieu android/gradlew.bat — cap add android that bai" }
    .\gradlew.bat assembleDebug
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
} finally {
    Pop-Location
}

$apk = Join-Path $frontend "android\app\build\outputs\apk\debug\app-debug.apk"
if (-not (Test-Path $apk)) {
    Write-Error "Khong tim thay APK: $apk"
}

Write-Host ""
Write-Host "APK: $apk" -ForegroundColor Green

if (Get-Command adb -ErrorAction SilentlyContinue) {
    $dev = adb devices | Select-String "device$"
    if ($dev) {
        Write-Host "=== 4/4 Cai qua USB ===" -ForegroundColor Cyan
        adb install -r $apk
        Write-Host "Da cai app 'Layla Care' tren dien thoai." -ForegroundColor Green
    } else {
        Write-Host "Khong thay dien thoai USB — copy APK sang may va cai tay, hoac bat USB debugging." -ForegroundColor Yellow
    }
} else {
    Write-Host "Chua co adb — copy APK vao dien thoai va mo file de cai." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "PC phai chay API: .\scripts\docker-up.ps1 (cung mang Wi-Fi voi dien thoai)" -ForegroundColor DarkGray
