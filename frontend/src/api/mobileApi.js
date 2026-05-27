/**
 * API app khách hàng (mobile / Layla) — sau đăng nhập, Bearer token bắt buộc.
 *
 * Chi tiết từng endpoint: JSDoc trên mỗi `export` bên dưới.
 */
import { getAccessToken } from "../auth/session";
import { apiUrl } from "./apiBase";

async function mobileFetch(path, options = {}) {
  const headers = {
    "Content-Type": "application/json",
    ...(options.headers || {})
  };
  const token = getAccessToken();
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(apiUrl(path), {
    ...options,
    headers
  });

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
 * GET /api/v1/mobile/home
 * Màn Tổng quan: gói đang dùng, % tiến độ (bài/ảnh/video), tiến độ từng dịch vụ, lịch tuần + task theo ngày.
 * @param {string} [selectedDate] ISO date (YYYY-MM-DD) — ngày đang chọn trên lịch
 * @usedBy composables/useAppScreen.js → HomeView
 */
export function getMobileHome(selectedDate) {
  const params = new URLSearchParams();
  if (selectedDate) {
    params.set("selectedDate", selectedDate);
  }
  const qs = params.toString();
  return mobileFetch(`/api/v1/mobile/home${qs ? `?${qs}` : ""}`);
}

/**
 * GET /api/v1/mobile/services
 * Màn Dịch vụ: gói active, danh sách hạng mục triển khai (có deliverableId nếu mở đánh giá), mô tả dịch vụ trong gói.
 * @param {'all'|'content'|'ads'|'report'} [category='all'] Lọc tab danh mục
 * @usedBy composables/useAppScreen.js → ServicesView
 */
export function getMobileServices(category = "all") {
  const params = new URLSearchParams();
  if (category) {
    params.set("category", category);
  }
  return mobileFetch(`/api/v1/mobile/services?${params}`);
}

/**
 * GET /api/v1/mobile/deliverables/{deliverableId}/review
 * Màn Đánh giá: thông tin bài giao (thumbnail, điểm team) + review nháp hoặc đã gửi.
 * @param {string} deliverableId UUID
 * @usedBy views/ServiceReviewView.vue
 */
export function getDeliverableReview(deliverableId) {
  return mobileFetch(`/api/v1/mobile/deliverables/${deliverableId}/review`);
}

/**
 * PUT /api/v1/mobile/deliverables/{deliverableId}/reviews/draft
 * Lưu nháp đánh giá (điểm chất lượng, nhận xét, gợi ý) — chưa chốt.
 * @usedBy views/ServiceReviewView.vue
 */
export function saveReviewDraft(deliverableId, body) {
  return mobileFetch(`/api/v1/mobile/deliverables/${deliverableId}/reviews/draft`, {
    method: "PUT",
    body: JSON.stringify(body)
  });
}

/**
 * POST /api/v1/mobile/deliverables/{deliverableId}/reviews
 * Gửi đánh giá (chốt, status SUBMITTED).
 * @usedBy views/ServiceReviewView.vue
 */
export function submitReview(deliverableId, body) {
  return mobileFetch(`/api/v1/mobile/deliverables/${deliverableId}/reviews`, {
    method: "POST",
    body: JSON.stringify(body)
  });
}

/**
 * GET /api/v1/mobile/notifications
 * Danh sách thông báo phân trang (FEEDBACK_REPLY, PROMOTION, SCHEDULE).
 * @param {number} [page=0]
 * @param {number} [size=20]
 * @param {boolean} [unreadOnly=false] Chỉ lấy chưa đọc
 * @usedBy views/NotificationsView.vue
 */
export function listNotifications(page = 0, size = 20, unreadOnly = false) {
  const params = new URLSearchParams({ page: String(page), size: String(size) });
  if (unreadOnly) params.set("unreadOnly", "true");
  return mobileFetch(`/api/v1/mobile/notifications?${params}`);
}

/**
 * GET /api/v1/mobile/notifications/unread-count
 * Số thông báo chưa đọc — badge icon chuông trên Home.
 * @usedBy views/HomeView.vue
 */
export function getUnreadNotificationCount() {
  return mobileFetch("/api/v1/mobile/notifications/unread-count");
}

/**
 * PATCH /api/v1/mobile/notifications/{id}/read
 * Đánh dấu một thông báo đã đọc (204).
 * @usedBy views/NotificationsView.vue
 */
export function markNotificationRead(id) {
  return mobileFetch(`/api/v1/mobile/notifications/${id}/read`, { method: "PATCH" });
}

/**
 * GET /api/v1/mobile/account
 * Màn Tài khoản: họ tên, SĐT, email, avatar, gói subscription hiện tại.
 * @usedBy views/AccountView.vue
 */
export function getMobileAccount() {
  return mobileFetch("/api/v1/mobile/account");
}

/**
 * PATCH /api/v1/mobile/account
 * Cập nhật thông tin cá nhân (fullName, phone, email, avatarUrl).
 * @usedBy views/AccountView.vue
 */
export function updateMobileAccount(body) {
  return mobileFetch("/api/v1/mobile/account", {
    method: "PATCH",
    body: JSON.stringify(body)
  });
}

/**
 * GET /api/v1/mobile/packages
 * Danh mục gói (code, tier, quota) — chọn gói khi gửi yêu cầu nâng cấp.
 * @usedBy views/AccountView.vue
 */
export function listAvailablePackages() {
  return mobileFetch("/api/v1/mobile/packages");
}

/**
 * POST /api/v1/mobile/package-upgrade-requests
 * Khách gửi yêu cầu nâng gói (toPackageCode, note) — admin duyệt ở adminApi.
 * @usedBy views/AccountView.vue
 */
export function requestPackageUpgrade(body) {
  return mobileFetch("/api/v1/mobile/package-upgrade-requests", {
    method: "POST",
    body: JSON.stringify(body)
  });
}

/**
 * GET /api/v1/mobile/vouchers
 * Voucher của user (mã, tiêu đề, hạn dùng, đã dùng hay chưa).
 * @usedBy views/AccountView.vue
 */
export function listMyVouchers() {
  return mobileFetch("/api/v1/mobile/vouchers");
}
