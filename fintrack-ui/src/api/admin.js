// src/api/admin.js
import api from "./client";

export async function fetchAdminOverview(userEmail, { signal } = {}) {
  const res = await api.get("/admin/overview", {
    headers: { "X-User-Email": userEmail },
    signal,
  });
  return res.data;
}
