# IP Wi-Fi de dien thoai cung mang truy cap (bo qua VPN / WSL)
$wifi = Get-NetIPAddress -AddressFamily IPv4 -ErrorAction SilentlyContinue |
    Where-Object {
        $_.InterfaceAlias -match "Wi-?Fi|WLAN" -and
        $_.IPAddress -notlike "127.*" -and
        $_.IPAddress -notlike "169.254.*"
    } |
    Select-Object -ExpandProperty IPAddress -First 1
if ($wifi) { return $wifi }

Get-NetIPAddress -AddressFamily IPv4 -ErrorAction SilentlyContinue |
    Where-Object {
        $_.IPAddress -notlike "127.*" -and
        $_.IPAddress -notlike "169.254.*" -and
        $_.InterfaceAlias -notmatch "WSL|Hyper-V|Pritunl|Loopback|vEthernet"
    } |
    Select-Object -ExpandProperty IPAddress -First 1
