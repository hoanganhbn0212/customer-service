import { getAccessToken } from "../auth/session";

async function adminFetch(path, options = {}) {
  const headers = {
    "Content-Type": "application/json",
    ...(options.headers || {})
  };
  const token = getAccessToken();
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(path, { ...options, headers });

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

export function listAdminPackages() {
  return adminFetch("/api/v1/admin/packages");
}

export function assignUserSubscription(userId, body) {
  return adminFetch(`/api/v1/admin/users/${userId}/subscription`, {
    method: "POST",
    body: JSON.stringify(body)
  });
}

export function listPackageUpgradeRequests(status) {
  const params = new URLSearchParams();
  if (status) {
    params.set("status", status);
  }
  const qs = params.toString();
  return adminFetch(`/api/v1/admin/package-upgrade-requests${qs ? `?${qs}` : ""}`);
}

export function reviewPackageUpgradeRequest(id, body) {
  return adminFetch(`/api/v1/admin/package-upgrade-requests/${id}`, {
    method: "PATCH",
    body: JSON.stringify(body)
  });
}
