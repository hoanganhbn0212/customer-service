# Customer Service Modules (API-first)

Du an duoc chia thanh 2 module:

- `frontend`: Vue.js (Vite)
- `backend`: Spring Boot (Maven)

Spec OpenAPI dat tai:

- `backend/src/main/resources/openapi/customer-service.yaml`

## Quy trinh API-first

1. Cap nhat API trong file spec OpenAPI.
2. Backend tu dong generate API interface + model khi build Maven.
3. Frontend generate API client tu chinh file spec.

## 1) PostgreSQL

Chay database bang Docker:

```bash
docker compose up -d
```

Thong tin ket noi mac dinh:

| Thuoc tinh | Gia tri |
|------------|---------|
| Host | `localhost` |
| Port | `5432` |
| Database | `customer_service` |
| User | `hydro_reader` |
| Password | `hydro_reader` |

Doi mat khau / URL: copy `backend/src/main/resources/application-local.yml.example` thanh `application-local.yml` va chinh sua.

## 2) Run Backend (Spring Boot + Maven)

Yeu cau: JDK 17+, Maven, PostgreSQL dang chay.

```bash
cd backend
.\mvnw.cmd clean spring-boot:run
```

(Khong can cai Maven global — `mvnw.cmd` tu tai Maven khi chay lan dau.)

Backend chay tai: `http://localhost:8082`  
Health API: `http://localhost:8082/api/health`

## 3) Generate frontend API client

Yeu cau: Node.js 18+.

```bash
cd frontend
npm install
npm run gen:api
```

Lenh `gen:api` doc spec tu backend va tao client vao `frontend/src/api` (ghi de client hien tai).

## 4) Run Frontend

```bash
cd frontend
npm run dev
```

Frontend chay tai: `http://localhost:5173`  
Frontend da cau hinh proxy `/api` sang backend `http://localhost:8082`.

## Script nhanh (Windows)

```powershell
# Terminal 1
.\scripts\run-backend.ps1

# Terminal 2
.\scripts\run-frontend.ps1
```

## API Auth (port tu module auth)

- `POST /api/v1/auth/login` — body: `{ "userName": "admin", "password": "password" }`
- `POST /api/v1/auth/register`
- `GET /api/v1/me`

User mac dinh (seed DB): **admin** / **password**

## API trong spec

- `GET /api/health`
- `GET /api/customers`
- `POST /api/customers`
- `GET /api/customers/{id}`
- `PUT /api/customers/{id}`
- `DELETE /api/customers/{id}`
