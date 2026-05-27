# Deploy miễn phí: Vercel (giao diện) + Render (API)

**Vercel free không chạy được Spring Boot + PostgreSQL.** Cách đúng:

| Phần | Nền tảng | Miễn phí |
|------|----------|----------|
| Vue frontend | **Vercel** | Có |
| Spring Boot API | **Render** | Có (có thể sleep khi không dùng) |
| PostgreSQL | **Render** | Có |

Người dùng mở **một URL Vercel** (`https://xxx.vercel.app`). Vercel proxy `/api` sang Render — không lỗi CORS.

---

## Phần 1 — Backend + DB trên Render (~10 phút)

1. Đẩy code lên **GitHub** (public hoặc private).
2. Vào [render.com](https://render.com) → đăng ký → **New** → **Blueprint**.
3. Connect repo `customer-service` → chọn file **`render.yaml`** ở root repo.
4. Deploy → đợi **customer-service-api** chuyển **Live**.
5. Copy URL API, ví dụ: `https://customer-service-api-xxxx.onrender.com`
6. Kiểm tra: mở `https://....onrender.com/api/health` → phải thấy JSON OK.

**Lưu ý free tier:** sau ~15 phút không truy cập, API **ngủ** — lần mở đầu có thể **chờ 30–60 giây**.

---

## Phần 2 — Frontend trên Vercel (~5 phút)

1. Vào [vercel.com](https://vercel.com) → đăng nhập GitHub.
2. **Add New Project** → import repo.
3. Cấu hình:

| Mục | Giá trị |
|-----|---------|
| **Root Directory** | `frontend` |
| **Framework Preset** | Vite (hoặc Other) |
| **Build Command** | `npm run build:vercel` |
| **Output Directory** | `dist` |
| **Install Command** | `npm install` |

4. **Environment Variables** (quan trọng):

| Name | Value |
|------|--------|
| `BACKEND_URL` | `https://customer-service-api-xxxx.onrender.com` (không có `/` cuối) |

5. **Deploy** → đợi build xong.
6. Mở URL Vercel, ví dụ `https://customer-service-xxx.vercel.app` — thử đăng nhập `admin` / `password`.

---

## Cập nhật sau này

- Sửa code → push GitHub → Vercel & Render tự build lại (nếu bật auto-deploy).
- Đổi URL Render → cập nhật lại `BACKEND_URL` trên Vercel → **Redeploy**.

---

## Sự cố

| Triệu chứng | Cách xử lý |
|-------------|------------|
| Trang Vercel load nhưng login/API lỗi | Kiểm tra `BACKEND_URL` đúng; Render API đang Live |
| API chậm lần đầu | Render free đang wake — đợi ~1 phút |
| Build Vercel lỗi `gen:api` | Root Directory phải là `frontend`, repo phải có thư mục `backend/.../openapi/` |
| 404 trên F5 trang con | `vercel.json` đã có SPA fallback — redeploy |

---

## Chỉ Vercel, không Render?

Không đủ cho app này (cần Java + Postgres). Có thể chỉ host **giao diện tĩnh** trên Vercel để xem UI, nhưng **đăng nhập / dữ liệu không hoạt động** nếu không có `BACKEND_URL`.

---

## So với VPS + domain

| | Vercel + Render | VPS (`deploy/deploy.sh`) |
|--|-----------------|---------------------------|
| Chi phí | $0 (giới hạn free) | Trả VPS + domain |
| HTTPS | Tự động | Caddy + domain |
| Backend ngủ | Có (Render free) | Không |
| Phù hợp | Demo, thử nhanh | Production ổn định |
