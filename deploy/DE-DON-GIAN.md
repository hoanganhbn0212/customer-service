# Đưa app lên mạng — giải thích đơn giản

## App gồm 2 phần

```
[ Trang web Vue ]  ----gọi API---->  [ Máy chủ Java + Database ]
     Vercel                              Render
  (miễn phí)                          (miễn phí)
```

- **Vercel** = chỗ chứa giao diện (nút bấm, màn hình).
- **Render** = chỗ chạy logic + lưu dữ liệu (đăng nhập, khách hàng…).

**Vercel một mình không đủ** — phải có Render trước.

---

## Làm 3 việc theo thứ tự

### Việc 1 — Đẩy code lên GitHub (5 phút)

Trên máy bạn, mở PowerShell:

```powershell
cd C:\Users\ADMIN\AnhDH\customer-service
git add .
git commit -m "cap nhat"
git push
```

Xong khi vào https://github.com/hoanganhbn0212/customer-service thấy code mới.

---

### Việc 2 — Bật “máy chủ” trên Render (10–15 phút)

1. Mở https://render.com → đăng nhập bằng **tài khoản GitHub**.
2. Bấm **New +** → chọn **Blueprint**.
3. Chọn repo **customer-service** → **Approve** / **Apply**.
4. Ngồi chờ đến khi dòng **customer-service-api** màu xanh **Live**.
5. Bấm vào service đó → copy **dòng URL** phía trên (dạng `https://xxx.onrender.com`).

**Thử:** dán URL đó vào trình duyệt, thêm `/api/health`  
Ví dụ: `https://xxx.onrender.com/api/health`  
→ Thấy chữ/json là được. **Giữ URL này.**

---

### Việc 3 — Bật “trang web” trên Vercel (5 phút)

1. Mở https://vercel.com → đăng nhập **GitHub**.
2. **Add New Project** → chọn **customer-service**.
3. Chỉ sửa **một chỗ**:

   **Root Directory** → gõ: `frontend`

   (Các ô khác để mặc định hoặc nếu hỏi thì: Build = `npm run build:vercel`, Output = `dist`)

4. Tìm chỗ **Environment Variables** (kéo xuống dưới, hoặc sau khi tạo xong vào **Settings**):

   | Ô trái (tên) | Ô phải (giá trị) |
   |--------------|------------------|
   | `BACKEND_URL` | Dán **URL Render việc 2** (không thêm `/` cuối) |

5. Bấm **Deploy** → chờ xong → bấm vào link `https://....vercel.app`.

**Đăng nhập thử:** `admin` / `password`

---

## Xong rồi dùng thế nào?

- Gửi link **vercel.app** cho mọi người (điện thoại mở được).
- Sửa code trên máy → `git push` → Vercel/Render tự cập nhật (vài phút).

---

## Hay hỏi

**Không thấy Environment Variables?**  
→ Vào project trên Vercel → **Settings** (bánh răng) → **Environment Variables** → thêm `BACKEND_URL` → **Redeploy**.

**Trang mở nhưng đăng nhập không được?**  
→ Chưa gắn Render: kiểm tra `BACKEND_URL` đúng URL bước 2.

**Lần đầu vào rất chậm?**  
→ Render free “ngủ”, đợi ~1 phút rồi thử lại.

**Chỉ muốn thử trên máy / điện thoại cùng Wi‑Fi?**  
→ Không cần Vercel. Chạy `.\scripts\start-thu-device.ps1` (xem `CHAY-THU.md`).

---

## Chỉ cần nhớ 2 link

| Link | Là gì |
|------|--------|
| `https://xxx.onrender.com` | API (bước 2) — dán vào Vercel |
| `https://yyy.vercel.app` | App cho user (bước 3) |
