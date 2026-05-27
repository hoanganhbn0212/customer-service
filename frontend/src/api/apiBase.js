/**
 * Cấu hình base URL cho mọi request API.
 *
 * - Web (Vite): để trống hoặc dùng proxy — path `/api/...` được proxy tới backend.
 * - Android (Capacitor): set `VITE_API_BASE_URL` = URL máy chủ, vd `http://192.168.1.10:8080`.
 *
 * @see {@link ./mobileApi.js} API app khách (sau đăng nhập)
 * @see {@link ./adminApi.js} API admin gói / tiến độ (chưa codegen)
 * @see {@link ./services/AuthService.ts} và các *Service — OpenAPI codegen
 */
export function getApiBase() {
  const base = (import.meta.env.VITE_API_BASE_URL || "").trim();
  return base.replace(/\/$/, "");
}

/** Ghép base + path (path luôn bắt đầu bằng `/`). */
export function apiUrl(path) {
  const p = path.startsWith("/") ? path : `/${path}`;
  const base = getApiBase();
  return base ? `${base}${p}` : p;
}
