# Prepare the Capacitor iOS project. Creating a signed IPA still requires macOS + Xcode.
$ErrorActionPreference = "Stop"

$root = Split-Path $PSScriptRoot -Parent
$frontend = Join-Path $root "frontend"
$envFile = Join-Path $frontend ".env.capacitor.local"
$envExample = Join-Path $frontend ".env.capacitor.example"
$isWindows = $env:OS -eq "Windows_NT"
$npm = if ($isWindows) { "npm.cmd" } else { "npm" }
$npx = if ($isWindows) { "npx.cmd" } else { "npx" }

Set-Location $frontend

if (-not (Test-Path $envFile)) {
    Copy-Item $envExample $envFile
    $ip = & (Join-Path $PSScriptRoot "get-wifi-ip.ps1")
    if ($ip) {
        (Get-Content $envFile) -replace "192.168.1.10", $ip | Set-Content $envFile
        Write-Host "Created .env.capacitor.local with Wi-Fi IP: $ip" -ForegroundColor Yellow
    } else {
        Write-Host "Edit frontend\.env.capacitor.local: VITE_API_BASE_URL=https://<public-api-domain>" -ForegroundColor Yellow
    }
}

Get-Content $envFile | ForEach-Object {
    if ($_ -match "^\s*VITE_API_BASE_URL\s*=\s*(.+)$") {
        $env:VITE_API_BASE_URL = $Matches[1].Trim()
    }
}
if (-not $env:VITE_API_BASE_URL) {
    Write-Error "Missing VITE_API_BASE_URL in .env.capacitor.local"
}

Write-Host "API URL for iPhone app: $env:VITE_API_BASE_URL" -ForegroundColor Cyan
Write-Host "For real iPhone distribution, prefer an HTTPS public API URL, not a LAN IP." -ForegroundColor Yellow

if (-not (Test-Path "node_modules")) {
    & $npm install
}

if (-not (Test-Path "node_modules\@capacitor\ios")) {
    Write-Host "Installing @capacitor/ios..." -ForegroundColor Cyan
    & $npm install @capacitor/ios@^6.2.0
}

Write-Host "=== 1/3 Build Vue for Capacitor ===" -ForegroundColor Cyan
& $npm run build:cap
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

if (-not (Test-Path "ios")) {
    Write-Host "=== 2/3 Add iOS project ===" -ForegroundColor Cyan
    & $npx cap add ios
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
} else {
    Write-Host "=== 2/3 iOS project exists ===" -ForegroundColor Cyan
}

Write-Host "=== 3/3 Capacitor sync iOS ===" -ForegroundColor Cyan
& $npx cap sync ios
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host ""
Write-Host "iOS project prepared: $frontend\ios" -ForegroundColor Green
Write-Host "Next step must run on macOS with Xcode:" -ForegroundColor Yellow
Write-Host "  cd frontend"
Write-Host "  npx cap open ios"
Write-Host "Then archive/sign and upload to TestFlight, or export an Ad Hoc IPA for a private HTTPS install link."
