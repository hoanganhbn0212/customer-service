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

## 1) Run Backend (Spring Boot + Maven)

Yeu cau: JDK 17+ va Maven.

```bash
cd backend
mvn clean spring-boot:run
```

Backend chay tai: `http://localhost:8080`  
Health API: `http://localhost:8080/api/health`

## 2) Generate frontend API client

Yeu cau: Node.js 18+.

```bash
cd frontend
npm install
npm run gen:api
```

Lenh `gen:api` doc spec tu backend va tao client vao `frontend/src/api` (ghi de client hien tai).

## 3) Run Frontend

```bash
cd frontend
npm run dev
```

Frontend chay tai: `http://localhost:5173`  
Frontend da cau hinh proxy `/api` sang backend `http://localhost:8080`.

## Script nhanh (Windows)

```powershell
# Terminal 1
.\scripts\run-backend.ps1

# Terminal 2
.\scripts\run-frontend.ps1
```

## API trong spec

- `GET /api/health`
- `GET /api/customers`
- `POST /api/customers`
- `GET /api/customers/{id}`
- `PUT /api/customers/{id}`
- `DELETE /api/customers/{id}`
