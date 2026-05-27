import { getAccessToken } from "../auth/session";

async function mobileFetch(path, options = {}) {
  const headers = {
    "Content-Type": "application/json",
    ...(options.headers || {})
  };
  const token = getAccessToken();
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(path, {
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

export function getMobileHome(selectedDate) {
  const params = new URLSearchParams();
  if (selectedDate) {
    params.set("selectedDate", selectedDate);
  }
  const qs = params.toString();
  return mobileFetch(`/api/v1/mobile/home${qs ? `?${qs}` : ""}`);
}

export function getMobileServices(category = "all") {
  const params = new URLSearchParams();
  if (category) {
    params.set("category", category);
  }
  return mobileFetch(`/api/v1/mobile/services?${params}`);
}

export function getDeliverableReview(deliverableId) {
  return mobileFetch(`/api/v1/mobile/deliverables/${deliverableId}/review`);
}

export function saveReviewDraft(deliverableId, body) {
  return mobileFetch(`/api/v1/mobile/deliverables/${deliverableId}/reviews/draft`, {
    method: "PUT",
    body: JSON.stringify(body)
  });
}

export function submitReview(deliverableId, body) {
  return mobileFetch(`/api/v1/mobile/deliverables/${deliverableId}/reviews`, {
    method: "POST",
    body: JSON.stringify(body)
  });
}

export function listNotifications(page = 0, size = 20, unreadOnly = false) {
  const params = new URLSearchParams({ page: String(page), size: String(size) });
  if (unreadOnly) params.set("unreadOnly", "true");
  return mobileFetch(`/api/v1/mobile/notifications?${params}`);
}

export function getUnreadNotificationCount() {
  return mobileFetch("/api/v1/mobile/notifications/unread-count");
}

export function markNotificationRead(id) {
  return mobileFetch(`/api/v1/mobile/notifications/${id}/read`, { method: "PATCH" });
}

export function getMobileAccount() {
  return mobileFetch("/api/v1/mobile/account");
}

export function updateMobileAccount(body) {
  return mobileFetch("/api/v1/mobile/account", {
    method: "PATCH",
    body: JSON.stringify(body)
  });
}

export function listAvailablePackages() {
  return mobileFetch("/api/v1/mobile/packages");
}

export function requestPackageUpgrade(body) {
  return mobileFetch("/api/v1/mobile/package-upgrade-requests", {
    method: "POST",
    body: JSON.stringify(body)
  });
}

export function listMyVouchers() {
  return mobileFetch("/api/v1/mobile/vouchers");
}
