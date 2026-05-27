# Chạy thử nhanh (Windows)

## Điện thoại xoay / load lâu?

Dùng **bản build** (nhanh hơn `npm run dev` rất nhiều):

```powershell
.\scripts\start-thu-device.ps1
```

Mở trên điện thoại: **`http://<IP-WiFi>:4173`** (không phải 5173).

Nếu vẫn không vào: chạy **PowerShell (Admin)**:

```powershell
.\scripts\allow-lan-firewall.ps1
```

---

## Một lệnh (dev trên PC)

```powershell
cd C:\Users\ADMIN\AnhDH\customer-service
.\scripts\start-thu.ps1
```

Script tự:

1. Bật PostgreSQL (`docker compose up -d`)
2. Mở backend (cửa sổ mới) nếu chưa chạy
3. Chạy frontend tại **http://localhost:5173**

| Trang | URL |
|-------|-----|
| App | http://localhost:5173 |
| Admin users | http://localhost:5173/admin/users |
| Điện thoại (cùng Wi‑Fi) | `http://<IP-máy>:5173` (script in IP) |

Đăng nhập: **admin** / **password**

---

## Deploy miễn phí online (Vercel + Render)

Xem **[deploy/VERCEL.md](deploy/VERCEL.md)** — frontend Vercel, API Render, không cần VPS.

---

## Chạy thử trên VPS (không domain)

Đã tạo sẵn `deploy/.env` (mật khẩu ngẫu nhiên). Trên Linux VPS:

```bash
cd deploy
chmod +x deploy-http.sh
./deploy-http.sh
```

Mở **http://IP-VPS** trên trình duyệt / điện thoại.

---

## Tắt khi xong

- Frontend: `Ctrl+C` trong terminal đang chạy `start-thu.ps1`
- Backend: đóng cửa sổ PowerShell backend, hoặc `taskkill` process Java port 8082
- Postgres: `docker compose down` (giữ data) hoặc `docker compose down -v` (xóa data)
