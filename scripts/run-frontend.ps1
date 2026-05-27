$frontend = Join-Path $PSScriptRoot "..\frontend"
Set-Location $frontend

if (-not (Get-Command npm -ErrorAction SilentlyContinue)) {
  Write-Error "Node/npm chua cai. Cai Node.js 18+ roi chay lai."
  exit 1
}

if (-not (Test-Path "node_modules")) {
  npm install
}

npm run gen:api
npm run dev
