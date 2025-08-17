// src/contexts/UserContext.jsx
import { createContext, useContext, useState, useEffect } from "react";

const UserContext = createContext();

// Defaults for DEV. Change role here to test: "EMPLOYEE" | "MANAGER" | "ADMIN"
let currentUserEmail = "evan.employee@demo.local";
let currentUserRoles = ["EMPLOYEE"]; // 👈 default role

export function UserProvider({ children }) {
  const [userEmail, setUserEmail] = useState(currentUserEmail);
  const [userRoles, setUserRoles] = useState(currentUserRoles);

  // keep module-level mirrors in sync for non-React code (e.g., axios interceptor)
  useEffect(() => {
    currentUserEmail = userEmail;
  }, [userEmail]);

  useEffect(() => {
    currentUserRoles = userRoles;
  }, [userRoles]);

  return (
    <UserContext.Provider value={{ userEmail, setUserEmail, userRoles, setUserRoles }}>
      {children}
    </UserContext.Provider>
  );
}

export function useUser() {
  const ctx = useContext(UserContext);
  if (!ctx) throw new Error("useUser must be used within UserProvider");
  return ctx;
}

// ✅ Helpers for non-component code
export function getUserEmail() {
  return currentUserEmail;
}

export function getUserRoles() {
  return currentUserRoles; // e.g., ["EMPLOYEE"] or ["MANAGER","ADMIN"]
}
