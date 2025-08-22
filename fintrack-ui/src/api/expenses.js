// fintrack-ui/src/api/expenses.js
import api from "./client";

/** Build headers for endpoints that require role checks */
function buildHeaders(userEmail, roles) {
  const rolesHeader =
    Array.isArray(roles) && roles.length ? roles.join(",") : "EMPLOYEE";
  return {
    "X-User-Email": userEmail,
    "X-User-Roles": rolesHeader,
  };
}

/** Employee's own expenses */
export async function fetchMyExpenses(userEmail, { signal, roles } = {}) {
  const res = await api.get("/expenses/mine", {
    headers: buildHeaders(userEmail, roles),
    signal,
  });
  return res.data;
}

/** Upload a single receipt; returns server URL like /uploads/<file> */
export async function uploadReceipt(file) {
  const formData = new FormData();
  formData.append("file", file);
  const res = await api.post("/expenses/upload", formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
  // backend returns { url, absoluteUrl }
  return res.data.url;
}

/** Manager/Admin queue — swallow 401/403 and return [] */
export async function fetchPendingExpenses(
  userEmail,
  { signal, roles } = {}
) {
  try {
    const res = await api.get("/expenses/pending", {
      headers: buildHeaders(userEmail, roles),
      signal,
    });
    return res.data;
  } catch (e) {
    if (e?.code === "ERR_CANCELED") throw e; // request was aborted

    const status = e?.response?.status;
    if (status === 401 || status === 403) {
      console.warn("Not authorized to view pending expenses → returning []");
      return [];
    }

    throw e; // unexpected error
  }
}

/** Create expense (employee) */
export async function createExpense(userEmail, payload, { roles } = {}) {
  const res = await api.post("/expenses", payload, {
    headers: buildHeaders(userEmail, roles),
  });
  return res.data;
}

/** Approve (manager/admin) */
export async function approveExpense(userEmail, id, { roles } = {}) {
  const res = await api.post(`/expenses/${id}/approve`, null, {
    headers: buildHeaders(userEmail, roles),
  });
  return res.data;
}

/** Reject (manager/admin) */
export async function rejectExpense(userEmail, id, { roles } = {}) {
  const res = await api.post(`/expenses/${id}/reject`, null, {
    headers: buildHeaders(userEmail, roles),
  });
  return res.data;
}
