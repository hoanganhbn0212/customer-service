# Chay PowerShell "Run as Administrator"
$ErrorActionPreference = "Stop"
$rules = @(
    @{ Name = "CustomerService-Vite-5173"; Port = 5173 },
    @{ Name = "CustomerService-Preview-4173"; Port = 4173 }
)
foreach ($r in $rules) {
    $existing = Get-NetFirewallRule -DisplayName $r.Name -ErrorAction SilentlyContinue
    if ($existing) {
        Write-Host "Da co rule: $($r.Name)"
        continue
    }
    New-NetFirewallRule -DisplayName $r.Name -Direction Inbound -Action Allow -Protocol TCP -LocalPort $r.Port | Out-Null
    Write-Host "Da mo port $($r.Port) — $($r.Name)" -ForegroundColor Green
}
Write-Host "Xong. Thu lai tren dien thoai."
