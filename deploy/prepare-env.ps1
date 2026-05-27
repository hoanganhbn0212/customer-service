# Tạo deploy/.env với mật khẩu ngẫu nhiên (chạy trên Windows trước khi đưa lên VPS)
$ErrorActionPreference = "Stop"
$here = Split-Path $MyInvocation.MyCommand.Path -Parent
$envFile = Join-Path $here ".env"
$example = Join-Path $here ".env.example"

if (Test-Path $envFile) {
  Write-Host "File .env da ton tai: $envFile"
  Write-Host "Xoa hoac doi ten neu muon tao moi."
  exit 0
}

function New-RandomHex([int]$bytes = 32) {
  $buf = New-Object byte[] $bytes
  [System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($buf)
  -join ($buf | ForEach-Object { $_.ToString("x2") })
}

$dbPass = New-RandomHex 24
$jwt = New-RandomHex 32

$content = @"
# Chinh DOMAIN va ACME_EMAIL truoc khi deploy
DOMAIN=app.example.com
ACME_EMAIL=admin@example.com

DB_USER=app
DB_PASSWORD=$dbPass

JWT_SECRET=$jwt
JWT_EXPIRATION_SECONDS=86400
"@

Set-Content -Path $envFile -Value $content -Encoding UTF8
Write-Host "Da tao: $envFile"
Write-Host "Sua DOMAIN va ACME_EMAIL, copy file len VPS (khong commit len Git)."
