# Hướng dẫn đưa app lên server (domain + điện thoại)

Sau khi deploy xong, mọi người mở **một link** kiểu `https://app.congty.vn` trên **điện thoại** hoặc máy tính — không cần cài app.

---

## Deploy miễn phí Vercel (không VPS)

Frontend trên **Vercel**, API + DB trên **Render** (free): xem **[VERCEL.md](VERCEL.md)**.

---

## Có bắt buộc domain thật không?


| Cách                         | Domain        | HTTPS | Điện thoại         | Mục đích                                              |
| ---------------------------- | ------------- | ----- | ------------------ | ----------------------------------------------------- |
| **Máy dev (LAN)**            | Không         | Không | Cùng Wi‑Fi         | `.\scripts\serve-lan.ps1` → `http://192.168.x.x:4173` |
| **Server thử (IP)**          | Không         | Không | Mở `http://IP_VPS` | `./deploy-http.sh` (xem mục dưới)                     |
| **Domain miễn phí / nip.io** | Có (miễn phí) | Có    | Có                 | Thử HTTPS không mua domain                            |
| **Production**               | Domain thật   | Có    | Có                 | `./deploy.sh`                                         |


**Không thể** dùng tên bừa kiểu `test.local` trên server public — trình duyệt/điện thoại không biết IP đó trỏ đâu (trừ khi sửa file `hosts` trên từng máy).

### Thử trên server không mua domain (chỉ HTTP)

Trên VPS, chỉ cần mở cổng **80**:

```bash
cd deploy
chmod +x deploy-http.sh
./deploy-http.sh
```

Mở: `**http://IP-public-VPS**` (vd. `http://203.0.113.10`). Điện thoại dùng 4G cũng vào được nếu firewall mở port 80.

Nhược điểm: không HTTPS, không nên để lâu dài / không gửi dữ liệu nhạy cảm.

### Thử HTTPS không mua domain (nip.io)

Nếu IP VPS là `203.0.113.10`, đặt trong `.env`:

```env
DOMAIN=203-0-113-10.nip.io
```

`nip.io` tự trỏ subdomain đó về IP. Chạy `./deploy.sh` như bình thường — Let's Encrypt **có thể** cấp chứng chỉ (đôi khi bị giới hạn / chặn).

---

## Bạn cần chuẩn bị (deploy production có HTTPS)


| Thứ       | Ví dụ                                       |
| --------- | ------------------------------------------- |
| VPS Linux | Ubuntu 22.04, RAM 2GB+                      |
| Domain    | `app.congty.vn` (hoặc nip.io như trên)      |
| DNS       | Bản ghi **A** → IP VPS (vd. `203.0.113.10`) |
| Email     | Dùng cho chứng chỉ HTTPS (Let's Encrypt)    |


Trên Windows chỉ cần **push code lên Git**; mọi thao tác build/deploy chạy **trên VPS**.

---

## Bước 1 — Cài Docker trên VPS (chỉ làm một lần)

SSH vào VPS:

```bash
ssh root@IP_VPS
```

Cài Docker (Ubuntu):

```bash
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER
# Đăng xuất SSH rồi vào lại để nhóm docker có hiệu lực
```

Kiểm tra:

```bash
docker --version
docker compose version
```

---

## Bước 2 — Đưa code lên VPS

```bash
git clone <URL-repo-cua-ban> customer-service
cd customer-service/deploy
```

(Nếu repo private: dùng SSH key hoặc token trên VPS.)

---

## Bước 3 — Tạo file cấu hình `.env`

```bash
cp .env.example .env
nano .env
```

Điền **đủ** các dòng (không để trống `JWT_SECRET`):

```env
DOMAIN=app.congty.vn
ACME_EMAIL=email-cua-ban@congty.vn

DB_USER=app
DB_PASSWORD=<mat-khau-manh-32-ky-tu>

JWT_SECRET=<chay-lenh-ben-duoi>
JWT_EXPIRATION_SECONDS=86400
```

Tạo `JWT_SECRET` trên VPS:

```bash
openssl rand -hex 32
```

Copy kết quả dán vào `JWT_SECRET=...`

**Lưu ý:** `DOMAIN` chỉ ghi tên miền, **không** có `https://`.

---

## Bước 4 — Kiểm tra trước khi deploy (khuyến nghị)

```bash
chmod +x preflight.sh deploy.sh
./preflight.sh
```

Script kiểm tra Docker, cổng 80/443, DNS trỏ đúng IP.

---

## Bước 5 — Chạy deploy

```bash
./deploy.sh
```

Lần đầu build **5–15 phút** (Maven + npm + Docker image).

Khi xong, mở trên điện thoại:

`**https://app.congty.vn`** (thay bằng domain của bạn)

Đăng nhập mặc định: `admin` / `password` → **đổi mật khẩu ngay**.

---

## Bước 6 — Cập nhật code sau này

Trên VPS:

```bash
cd ~/customer-service
git pull
cd deploy
docker compose -f docker-compose.prod.yml --env-file .env up -d --build
```

---

## Lệnh xử lý sự cố

```bash
cd deploy

# Xem log tất cả
docker compose -f docker-compose.prod.yml logs -f

# Chỉ backend
docker compose -f docker-compose.prod.yml logs -f backend

# Chỉ web (Caddy + giao diện)
docker compose -f docker-compose.prod.yml logs -f web

# Trạng thái container
docker compose -f docker-compose.prod.yml ps

# Dừng hẳn
docker compose -f docker-compose.prod.yml down
```


| Lỗi                 | Nguyên nhân thường gặp                                           |
| ------------------- | ---------------------------------------------------------------- |
| Không mở được HTTPS | DNS chưa trỏ IP VPS; chưa mở port 80/443 trên firewall VPS/cloud |
| 502 Bad Gateway     | Backend chưa lên — xem `logs backend`                            |
| Trang trắng         | Lỗi build frontend — xem `logs web`                              |
| Certificate pending | Đợi 5–30 phút sau khi DNS đúng, rồi `docker compose restart web` |


---

## Firewall (VPS + nhà cung cấp cloud)

Mở inbound:

- **TCP 80** (HTTP — Let's Encrypt cần)
- **TCP 443** (HTTPS)

Ubuntu `ufw` ví dụ:

```bash
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw allow OpenSSH
sudo ufw enable
```

Trên **DigitalOcean / AWS / Azure / Viettel Cloud**: thêm rule Security Group / Firewall tương tự.

---

## Tạo `.env` từ máy Windows (tùy chọn)

Trước khi copy lên VPS, trên PC:

```powershell
cd C:\Users\ADMIN\AnhDH\customer-service\deploy
.\prepare-env.ps1
```

Sửa `DOMAIN` và `ACME_EMAIL` trong file `.env` vừa tạo, rồi copy lên VPS (không commit `.env` lên Git).

---

## Sơ đồ hoạt động

```
Điện thoại / PC
    → https://DOMAIN (Caddy, cổng 443)
        → /        → file Vue (giao diện app)
        → /api/*   → Spring Boot (backend)
            → PostgreSQL (trong Docker, không ra internet)
```

Mọi API gọi qua cùng domain nên **không lỗi CORS** trên production.