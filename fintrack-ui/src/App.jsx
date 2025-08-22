import ExpenseForm from "./components/ExpenseForm";
import ExpenseList from "./components/ExpenseList";
import ManagerQueue from "./components/ManagerQueue";
import useCurrentUser from "./hooks/UseCurrentUser";
import AdminDashboard from "./components/AdminDashboard";
import { useUser } from "./contexts/UserContext";

function App() {
  const { userEmail, setUserEmail, setUserRoles } = useUser();
  const user = useCurrentUser(userEmail); // refetches on change

  if (!user) return <p>Loading user info...</p>;

  const hasRole = (role) => user?.roles?.includes(role);

  const onSelectUser = (e) => {
    const email = e.target.value;
    // ✅ set roles first, then email — avoids one-tick 403s
    if (email === "evan.employee@demo.local") setUserRoles(["EMPLOYEE"]);
    else if (email === "alice.manager@demo.local") setUserRoles(["MANAGER"]);
    else if (email === "ada.admin@demo.local") setUserRoles(["ADMIN"]);
    else setUserRoles(["EMPLOYEE"]);

    setUserEmail(email);
  };

  return (
    <div>
      <h1>FinTrack</h1>

      <div style={{ marginBottom: 20 }}>
        <label>
          <strong>Select User:</strong>{" "}
          <select value={userEmail} onChange={onSelectUser}>
            <option value="evan.employee@demo.local">Evan (Employee)</option>
            <option value="alice.manager@demo.local">Alice (Manager)</option>
            <option value="ada.admin@demo.local">Ada (Admin)</option>
          </select>
        </label>
      </div>

      {/* EMPLOYEE: submit + see own expenses */}
      {hasRole("EMPLOYEE") && (
        <>
          <ExpenseForm />
          <ExpenseList />
        </>
      )}

      {/* MANAGER + ADMIN: approvals queue */}
      {(hasRole("MANAGER") || hasRole("ADMIN")) && (
        <ManagerQueue userEmail={userEmail} roles={user.roles} />
      )}

      {/* ADMIN: dashboard */}
      {hasRole("ADMIN") && (
        <AdminDashboard userEmail={userEmail} roles={user.roles} enabled />
      )}
    </div>
  );
}

export default App;
