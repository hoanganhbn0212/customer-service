# Build Android APK and optionally install via USB/adb.
$ErrorActionPreference = "Stop"

$root = Split-Path $PSScriptRoot -Parent
$frontend = Join-Path $root "frontend"
$envFile = Join-Path $frontend ".env.capacitor.local"
$envExample = Join-Path $frontend ".env.capacitor.example"
$jdk17Home = "C:\Program Files\Microsoft\jdk-17.0.19.10-hotspot"

if (Test-Path (Join-Path $jdk17Home "bin\java.exe")) {
    $env:JAVA_HOME = $jdk17Home
    $env:Path = (Join-Path $jdk17Home "bin") + [IO.Path]::PathSeparator + $env:Path
}

Set-Location $frontend

if (-not (Test-Path $envFile)) {
    Copy-Item $envExample $envFile
    $ip = & (Join-Path $PSScriptRoot "get-wifi-ip.ps1")
    if ($ip) {
        (Get-Content $envFile) -replace "192.168.1.10", $ip | Set-Content $envFile
        Write-Host "Created .env.capacitor.local with Wi-Fi IP: $ip" -ForegroundColor Yellow
    } else {
        Write-Host "Edit frontend\.env.capacitor.local: VITE_API_BASE_URL=http://<PC-IP>:8080" -ForegroundColor Yellow
    }
    Write-Host "Docker app must be running: .\scripts\docker-up.ps1" -ForegroundColor Yellow
}

Get-Content $envFile | ForEach-Object {
    if ($_ -match "^\s*VITE_API_BASE_URL\s*=\s*(.+)$") {
        $env:VITE_API_BASE_URL = $Matches[1].Trim()
    }
}
if (-not $env:VITE_API_BASE_URL) {
    Write-Error "Missing VITE_API_BASE_URL in .env.capacitor.local"
}

Write-Host "API URL for app: $env:VITE_API_BASE_URL" -ForegroundColor Cyan

if (-not (Test-Path "node_modules")) {
    npm install
}

Write-Host "=== 1/4 Build Vue for Capacitor ===" -ForegroundColor Cyan
npm run build:cap
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

if (-not (Test-Path "android")) {
    Write-Host "=== Add Android project ===" -ForegroundColor Cyan
    npx cap add android
}

Write-Host "=== 2/4 Capacitor sync ===" -ForegroundColor Cyan
npx cap sync android
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$manifest = Join-Path $frontend "android\app\src\main\AndroidManifest.xml"
if (Test-Path $manifest) {
    $xml = Get-Content $manifest -Raw
    if ($xml -notmatch "usesCleartextTraffic") {
        $xml = $xml -replace "<application", '<application android:usesCleartextTraffic="true"'
        Set-Content $manifest $xml -NoNewline
        Write-Host "Enabled usesCleartextTraffic for local HTTP API" -ForegroundColor DarkGray
    }
}

Write-Host "=== 3/4 Build APK ===" -ForegroundColor Cyan
Push-Location android
try {
    if (-not (Test-Path "gradlew.bat")) {
        throw "Missing android/gradlew.bat; cap add android failed"
    }
    .\gradlew.bat assembleDebug
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
} finally {
    Pop-Location
}

$apk = Join-Path $frontend "android\app\build\outputs\apk\debug\app-debug.apk"
if (-not (Test-Path $apk)) {
    Write-Error "APK not found: $apk"
}

Write-Host ""
Write-Host "APK: $apk" -ForegroundColor Green

if (Get-Command adb -ErrorAction SilentlyContinue) {
    $dev = adb devices | Select-String "device$"
    if ($dev) {
        Write-Host "=== 4/4 Install via USB ===" -ForegroundColor Cyan
        adb install -r $apk
        if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
        Write-Host "Installed app 'Layla Care' on the connected phone." -ForegroundColor Green
    } else {
        Write-Host "No USB phone detected. Copy the APK to the phone and install manually, or enable USB debugging." -ForegroundColor Yellow
    }
} else {
    Write-Host "adb not found. Copy the APK to the phone and open it to install." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "PC must keep running API: .\scripts\docker-up.ps1, on the same Wi-Fi as the phone." -ForegroundColor DarkGray
