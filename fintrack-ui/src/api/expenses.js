// fintrack-ui/src/api/expenses.js
import api from "./client";
import { getUserRoles } from "../contexts/UserContext";

// Build headers for endpoints that require role checks
function buildHeaders(userEmail) {
  const roles = getUserRoles?.();
  const rolesHeader = Array.isArray(roles) && roles.length ? roles.join(",") : "EMPLOYEE";
  return {
    "X-User-Email": userEmail,
    "X-User-Roles": rolesHeader,
  };
}

/**
 * Each function returns the parsed response data.
 */

export async function fetchMyExpenses(userEmail) {
  const res = await api.get("/expenses/mine", {
    headers: buildHeaders(userEmail),
  });
  return res.data;
}

export async function fetchPendingExpenses(userEmail) {
  const res = await api.get("/expenses/pending", {
    headers: buildHeaders(userEmail),
  });
  return res.data;
}

export async function createExpense(userEmail, payload) {
  const res = await api.post("/expenses", payload, {
    headers: buildHeaders(userEmail),
  });
  return res.data;
}

export async function approveExpense(userEmail, id) {
  const res = await api.post(`/expenses/${id}/approve`, null, {
    headers: buildHeaders(userEmail),
  });
  return res.data;
}

export async function rejectExpense(userEmail, id) {
  const res = await api.post(`/expenses/${id}/reject`, null, {
    headers: buildHeaders(userEmail),
  });
  return res.data;
}
