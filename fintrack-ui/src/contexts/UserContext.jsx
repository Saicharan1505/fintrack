// src/contexts/UserContext.jsx
import { createContext, useContext, useState, useEffect } from "react";

const UserContext = createContext();

// Default (employee)
let currentUserEmail = "evan.employee@demo.local";
let currentUserRoles = ["EMPLOYEE"];

export function UserProvider({ children }) {
  const [userEmail, setUserEmail] = useState(currentUserEmail);
  const [userRoles, setUserRoles] = useState(currentUserRoles);

  const [expensesVersion, setExpensesVersion] = useState(0);
  const bumpExpenses = () => setExpensesVersion((v) => v + 1);

  // keep global mirrors in sync
  useEffect(() => {
    currentUserEmail = userEmail;
  }, [userEmail]);

  useEffect(() => {
    currentUserRoles = userRoles;
  }, [userRoles]);

  // --- helper to change user (email + roles together) ---
  function switchUser(email) {
    let roles = ["EMPLOYEE"]; // fallback
    if (email.includes("manager")) {
      roles = ["MANAGER"];
    } else if (email.includes("admin")) {
      roles = ["ADMIN"];
    }
    setUserEmail(email);
    setUserRoles(roles);
  }

  return (
    <UserContext.Provider value={{ userEmail, userRoles, setUserEmail, setUserRoles, switchUser, expensesVersion, bumpExpenses,

     }}>
      {children}
    </UserContext.Provider>
  );
}

export function useUser() {
  const ctx = useContext(UserContext);
  if (!ctx) throw new Error("useUser must be used within UserProvider");
  return ctx;
}

// Non-React helpers
export function getUserEmail() {
  return currentUserEmail;
}

export function getUserRoles() {
  return currentUserRoles;
}
