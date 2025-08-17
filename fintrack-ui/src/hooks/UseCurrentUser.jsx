import { useEffect, useState } from "react";
import { getUserRoles } from "../contexts/UserContext";

export default function useCurrentUser(userEmail) {
  const [user, setUser] = useState(null);

  useEffect(() => {
    if (!userEmail) return;
    const roles = getUserRoles?.();
    const rolesHeader = Array.isArray(roles) && roles.length ? roles.join(",") : "EMPLOYEE";

    fetch("http://localhost:8080/api/me", {
      headers: {
        "X-User-Email": userEmail,
         "X-User-Roles": rolesHeader
      }
    })
      .then(res => res.json())
      .then(data => {
        console.log("✅ Current user response:", data);
        setUser(data);
      });
  }, [userEmail]); // 💥 Key: this runs again whenever userEmail changes

  return user;
}
