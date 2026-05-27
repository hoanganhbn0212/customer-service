# App Android (APK) — Capacitor

Giao diện Vue được đóng gói thành app **Layla Care**, cài qua **cáp USB** (hoặc copy file APK).

Backend vẫn chạy trên **PC (Docker)** hoặc server — điện thoại gọi API qua Wi‑Fi.

---

## Chuẩn bị (một lần)

1. **Docker Desktop** — chạy app trên PC  
2. **Node.js 18+**  
3. **Android Studio** (hoặc chỉ [Platform Tools](https://developer.android.com/tools/releases/platform-tools) + **JDK 17**)  
4. Điện thoại Android: bật **USB debugging**

---

## Bước 1 — Bật API trên PC

```powershell
cd C:\Users\ADMIN\AnhDH\customer-service
.\scripts\docker-up.ps1
```

Thử trên PC: http://localhost:8080/api/health → OK

---

## Bước 2 — Cấu hình IP (lần đầu)

File `frontend\.env.capacitor.local` (script tự tạo từ example):

```env
VITE_API_BASE_URL=http://172.16.252.97:8080
```

Thay `172.16.252.97` bằng **IPv4 Wi‑Fi** của PC (`ipconfig`).

Điện thoại và PC **cùng Wi‑Fi**.

---

## Bước 3 — Build APK + cài USB

```powershell
cd C:\Users\ADMIN\AnhDH\customer-service
.\scripts\build-android.ps1
```

Lần đầu có thể **10–20 phút** (tải Gradle/Android).

- Thành công → app **Layla Care** trên điện thoại  
- Đăng nhập: `admin` / `password`

---

## Chỉ build APK (không cài USB)

```powershell
cd frontend
npm install
# dat VITE_API_BASE_URL trong .env.capacitor.local
npm run build:cap
npx cap sync android
cd android
.\gradlew.bat assembleDebug
```

APK: `frontend\android\app\build\outputs\apk\debug\app-debug.apk`

---

## Sự cố

| Triệu chứng | Cách xử lý |
|-------------|------------|
| App mở nhưng login lỗi | Sai IP trong `.env.capacitor.local`; Docker chưa chạy; khác Wi‑Fi |
| `gradlew` lỗi | Cài JDK 17; mở Android Studio một lần để tải SDK |
| `adb` không thấy máy | Bật USB debugging; cáp data; chấp nhận RSA fingerprint |
| Đổi IP Wi‑Fi | Sửa `.env.capacitor.local` → chạy lại `build-android.ps1` |

---

## iPhone

Capacitor hỗ trợ iOS nhưng cần **Mac + Xcode**. Repo hiện chỉ script **Android**.
