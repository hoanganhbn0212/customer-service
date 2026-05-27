# Form deploy — copy từng ô vào Vercel / Render

> Điền **Value thật** vào các ô `____________` sau khi deploy Render (bước 1).  
> Repo GitHub: `https://github.com/hoanganhbn0212/customer-service`

---

## Bước 0 — GitHub (push code trước)

```powershell
cd C:\Users\ADMIN\AnhDH\customer-service
git add .
git commit -m "Deploy Vercel + Render"
git push origin main
```

---

## Bước 1 — Render.com

**Vào:** https://render.com → **New** → **Blueprint** → chọn repo `hoanganhbn0212/customer-service`

| Ô trên Render | Điền |
|---------------|------|
| Blueprint file | `render.yaml` (tự nhận ở root repo) |
| (Không cần điền env tay — file yaml đã khai báo) | |

**Sau khi Live**, copy URL service (Settings → URL):

| Biến bạn ghi nhớ | Value (điền sau khi Live) |
|------------------|---------------------------|
| **RENDER_API_URL** | `https://________________.onrender.com` |

**Kiểm tra** — mở trình duyệt:

```
https://________________.onrender.com/api/health
```

Phải thấy JSON (status OK).

---

## Bước 2 — Vercel.com

**Vào:** https://vercel.com → **Add New** → **Project** → repo `customer-service`

### Tab General / Build

| Mục trên Vercel | Copy dán chính xác |
|-----------------|-------------------|
| **Root Directory** | `frontend` |
| **Framework Preset** | `Vite` hoặc `Other` |
| **Install Command** | `npm install` |
| **Build Command** | `npm run build:vercel` |
| **Output Directory** | `dist` |

### Environment Variables (kéo xuống hoặc Settings → Environment Variables)

| Key | Value | Environment |
|-----|-------|-------------|
| `BACKEND_URL` | *(dán **RENDER_API_URL** — không có `/` cuối)* | Production, Preview, Development |

**Ví dụ Value:**

```
https://customer-service-api-xxxxx.onrender.com
```

### Sau khi thêm biến

- Bấm **Deploy**
- Nếu đã deploy trước khi có `BACKEND_URL` → **Deployments** → **Redeploy**

**URL app sau deploy:**

| Biến | Value |
|------|-------|
| **VERCEL_APP_URL** | `https://________________.vercel.app` |

**Đăng nhập thử:**

| Ô | Value |
|---|-------|
| User | `admin` |
| Password | `password` |

---

## Form tổng hợp (điền một lần, giữ lại)

| # | Tên | Value của bạn |
|---|-----|----------------|
| 1 | GitHub repo | `https://github.com/hoanganhbn0212/customer-service` |
| 2 | RENDER_API_URL | `https://________________.onrender.com` |
| 3 | BACKEND_URL (Vercel) | *cùng giá trị dòng 2* |
| 4 | VERCEL_APP_URL | `https://________________.vercel.app` |
| 5 | Health check | `https://________________.onrender.com/api/health` |
| 6 | App login | `admin` / `password` |

---

## Copy nhanh — chỉ Vercel (đã có Render URL)

Thay `PASTE_RENDER_URL` bằng URL Render thật:

**Root Directory**
```
frontend
```

**Install Command**
```
npm install
```

**Build Command**
```
npm run build:vercel
```

**Output Directory**
```
dist
```

**Environment Variable — Key**
```
BACKEND_URL
```

**Environment Variable — Value**
```
PASTE_RENDER_URL
```

---

## Lỗi thường gặp

| Triệu chứng | Sửa |
|-------------|-----|
| Không thấy Environment Variables | Project → **Settings** → **Environment Variables** → Add → **Redeploy** |
| Login / API lỗi | `BACKEND_URL` sai hoặc thiếu; Render chưa Live |
| Trang trắng / 404 F5 | Redeploy sau khi sửa `BACKEND_URL` |
| Chậm 30–60s lần đầu | Render free đang wake — đợi rồi thử lại |
