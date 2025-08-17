import axios from "axios";
import { getUserEmail, getUserRoles } from "../contexts/UserContext";

const api = axios.create({
  // Use your existing env var or fallback to the backend prefix
  baseURL: import.meta.env.VITE_API_BASE_URL || "/api",
  headers: {
    "Content-Type": "application/json",
  },
});

// Inject identity headers on every request (DEV)
api.interceptors.request.use(
  (config) => {
    const userEmail = getUserEmail?.();
    if (userEmail) {
      config.headers["X-User-Email"] = userEmail;
    }

    // NEW: send roles (comma separated). Fallback to EMPLOYEE for dev.
    const roles = getUserRoles?.();
    config.headers["X-User-Roles"] = Array.isArray(roles) && roles.length > 0
      ? roles.join(",")             // e.g. "EMPLOYEE" or "MANAGER,ADMIN"
      : "EMPLOYEE";                 // default for local testing

    return config;
  },
  (error) => Promise.reject(error)
);

export default api;
