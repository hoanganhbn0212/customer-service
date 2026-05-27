/**
 * API admin — gói dịch vụ, gán subscription, duyệt nâng cấp, cập nhật tiến độ.
 * Chưa sinh từ OpenAPI; gọi trực tiếp bằng fetch + Bearer (role ADMIN).
 *
 * Chi tiết từng endpoint: JSDoc trên mỗi `export` bên dưới.
 */
import { getAccessToken } from "../auth/session";
import { apiUrl } from "./apiBase";

async function adminFetch(path, options = {}) {
  const headers = {
    "Content-Type": "application/json",
    ...(options.headers || {})
  };
  const token = getAccessToken();
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(apiUrl(path), { ...options, headers });

  if (!response.ok) {
    const err = new Error(`API ${response.status}`);
    err.status = response.status;
    try {
      err.body = await response.json();
    } catch {
      /* ignore */
    }
    throw err;
  }

  if (response.status === 204) {
    return null;
  }
  return response.json();
}

/**
 * GET /api/v1/admin/packages
 * Danh mục gói (code, tier BASIC/PRO, quota bài/ảnh/video) — form gán gói & hiển thị label.
 * @usedBy views/SubscriptionsAdminView.vue
 */
export function listAdminPackages() {
  return adminFetch("/api/v1/admin/packages");
}

/**
 * POST /api/v1/admin/users/{userId}/subscription
 * Gán gói active cho user (packageCode, displayTitle, startDate, endDate).
 * @param {string} userId
 * @usedBy views/SubscriptionsAdminView.vue — tab “Gán gói”
 */
export function assignUserSubscription(userId, body) {
  return adminFetch(`/api/v1/admin/users/${userId}/subscription`, {
    method: "POST",
    body: JSON.stringify(body)
  });
}

/**
 * GET /api/v1/admin/package-upgrade-requests
 * Danh sách yêu cầu nâng cấp từ app khách (AccountView).
 * @param {'PENDING'|'APPROVED'|'REJECTED'} [status] Lọc trạng thái
 * @usedBy views/SubscriptionsAdminView.vue — tab duyệt yêu cầu
 */
export function listPackageUpgradeRequests(status) {
  const params = new URLSearchParams();
  if (status) {
    params.set("status", status);
  }
  const qs = params.toString();
  return adminFetch(`/api/v1/admin/package-upgrade-requests${qs ? `?${qs}` : ""}`);
}

/**
 * PATCH /api/v1/admin/package-upgrade-requests/{id}
 * Duyệt (APPROVED) hoặc từ chối (REJECTED) kèm adminNote.
 * Khi duyệt, backend thường kích hoạt gói mới cho user.
 * @usedBy views/SubscriptionsAdminView.vue
 */
export function reviewPackageUpgradeRequest(id, body) {
  return adminFetch(`/api/v1/admin/package-upgrade-requests/${id}`, {
    method: "PATCH",
    body: JSON.stringify(body)
  });
}

/**
 * GET /api/v1/admin/subscription-progress
 * Tiến độ triển khai theo subscription (bài/ảnh/video đã làm vs quota).
 * @param {string} [packageCode] Lọc theo mã gói
 * @usedBy views/ProgressAdminView.vue
 */
export function listSubscriptionProgress(packageCode) {
  const params = new URLSearchParams();
  if (packageCode) params.set("packageCode", packageCode);
  const qs = params.toString();
  return adminFetch(`/api/v1/admin/subscription-progress${qs ? `?${qs}` : ""}`);
}

/**
 * PATCH /api/v1/admin/subscription-progress/{subscriptionId}
 * Admin cập nhật số liệu tiến độ (completedPosts/Images/Videos, …) — đồng bộ lên mobile/home.
 * @usedBy views/ProgressAdminView.vue
 */
export function updateSubscriptionProgress(subscriptionId, body) {
  return adminFetch(`/api/v1/admin/subscription-progress/${subscriptionId}`, {
    method: "PATCH",
    body: JSON.stringify(body)
  });
}
