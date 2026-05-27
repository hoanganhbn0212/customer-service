# Chạy app bằng Docker (không cần Vercel / npm / Maven trên máy)

Một lệnh chạy **database + API + giao diện web**.

## Yêu cầu

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (bật Docker Engine)

## Chạy

**Windows (PowerShell):**

```powershell
cd C:\Users\ADMIN\AnhDH\customer-service
.\scripts\docker-up.ps1
```

**Hoặc:**

```powershell
docker compose up -d --build
```

## Mở app

| Thiết bị | URL |
|----------|-----|
| Máy tính | http://localhost:8080/ |
| Điện thoại (cùng Wi‑Fi) | http://\<IP-máy-tính\>:8080/ |

Đăng nhập: **admin** / **password**

Đổi cổng: sửa `APP_PORT=8080` trong file `.env` (copy từ `.env.example`).

---

## Cắm USB Android (không cần Wi‑Fi)

App này là **trang web**, không phải file `.apk` cài trực tiếp. Với **Android** có thể dùng cổng USB:

1. Bật **USB debugging** trên điện thoại (Developer options).
2. Cài [Android Platform Tools](https://developer.android.com/tools/releases/platform-tools) (`adb`).
3. PC chạy Docker: `.\scripts\docker-up.ps1`
4. Chạy:

```powershell
.\scripts\phone-usb-android.ps1
```

5. Trên điện thoại mở Chrome: **http://localhost:8080/**

**iPhone:** không dùng `adb` — dùng cùng Wi‑Fi (`http://<IP-PC>:8080`) hoặc cần build app native (Capacitor).

### Muốn app cài như APK (icon trên màn hình)?

Cần thêm **Capacitor** (bọc bản build Vue thành Android/iOS). Chưa có trong repo — có thể làm thêm nếu bạn cần.

---

## Lệnh thường dùng

```powershell
# Xem log
docker compose logs -f

# Chỉ backend
docker compose logs -f backend

# Dừng (giữ dữ liệu DB)
docker compose down

# Dừng + xóa database
docker compose down -v

# Build lại sau khi sửa code
docker compose up -d --build
```

---

## Kiến trúc

```
Trình duyệt / điện thoại
    → http://localhost:8080  (container web - Caddy + Vue build)
        → /api/*  → backend:8082 (Spring Boot)
        → postgres (PostgreSQL)
```

---

## Sự cố

| Triệu chứng | Cách xử lý |
|-------------|------------|
| Build lâu | Lần đầu tải image + Maven + npm — đợi 10–15 phút |
| Không mở được trên điện thoại | Firewall Windows — cho phép Docker; cùng Wi‑Fi |
| Port 8080 bị chiếm | Đổi `APP_PORT=8888` trong `.env` rồi `docker compose up -d --build` |
| Backend lỗi | `docker compose logs backend` |

---

## Dev chỉ database (tùy chọn)

Nếu vẫn muốn chạy `npm run dev` + `mvnw` trên máy, chỉ bật Postgres:

```powershell
docker compose up -d postgres
```

(Rồi dùng `.\scripts\start-thu.ps1` như trước.)
