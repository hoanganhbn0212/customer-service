# Ensures PostgreSQL (Docker) is running and accepting connections before starting the backend.
param(
    [int]$TimeoutSeconds = 90
)

$ErrorActionPreference = "Stop"
$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$composeFile = Join-Path $root "docker-compose.yml"

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Error @"
Docker is not installed or not on PATH.
Install Docker Desktop, then run from project root:
  docker compose up -d postgres
"@
    exit 1
}

Write-Host "Starting PostgreSQL (docker compose)..." -ForegroundColor Cyan
Push-Location $root
try {
    docker compose -f $composeFile up -d postgres | Out-Host
    if ($LASTEXITCODE -ne 0) {
        throw "docker compose up failed (exit $LASTEXITCODE)"
    }
}
finally {
    Pop-Location
}

$deadline = (Get-Date).AddSeconds($TimeoutSeconds)
$ready = $false

while ((Get-Date) -lt $deadline) {
    try {
        $out = docker exec customer-service-postgres pg_isready -U hydro_reader -d customer_service 2>&1
        if ($LASTEXITCODE -eq 0 -and ($out -match "accepting connections")) {
            $ready = $true
            break
        }
    }
    catch {
        # container may still be starting
    }

    $portOpen = $false
    try {
        $portOpen = (Test-NetConnection -ComputerName localhost -Port 5432 -WarningAction SilentlyContinue).TcpTestSucceeded
    }
    catch {
        $portOpen = $false
    }

    if ($portOpen) {
        $ready = $true
        break
    }

    Write-Host "Waiting for PostgreSQL on localhost:5432..." -ForegroundColor DarkYellow
    Start-Sleep -Seconds 2
}

if (-not $ready) {
    Write-Error @"
PostgreSQL did not become ready within ${TimeoutSeconds}s.
Check: docker ps -a --filter name=customer-service-postgres
       docker logs customer-service-postgres
"@
    exit 1
}

Write-Host "PostgreSQL is ready (localhost:5432 / customer_service)." -ForegroundColor Green
