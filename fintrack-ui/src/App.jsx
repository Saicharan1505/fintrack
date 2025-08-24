import { useState } from "react"; 
import ExpenseForm from "./components/ExpenseForm";
import ExpenseList from "./components/ExpenseList";
import ManagerQueue from "./components/ManagerQueue";
import useCurrentUser from "./hooks/UseCurrentUser";
import AdminDashboard from "./components/AdminDashboard";
import { useUser } from "./contexts/UserContext";

function App() {
  const { userEmail, switchUser } = useUser();  // 👈 use switchUser
  const [reload, setReload] = useState(0);
  const user = useCurrentUser(userEmail);       // refetches on change

  if (!user) return <p>Loading user info...</p>;

  const hasRole = (role) => user?.roles?.includes(role);

  const onSelectUser = (e) => {
    const email = e.target.value;
    switchUser(email); // 👈 handles both email + roles
  };

  const handleExpenseCreated = () => {
    setReload((r) => r + 1); // increment → triggers ExpenseList useEffect
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

      {hasRole("EMPLOYEE") && (
        <>
          <ExpenseForm onCreated={handleExpenseCreated} />
          <ExpenseList reloadTrigger={reload} />
        </>
      )}

      {/* MANAGER ONLY: approvals queue */}
      {hasRole("MANAGER") && (
        <ManagerQueue userEmail={userEmail} roles={user.roles} />
      )}

      {/* ADMIN ONLY: dashboard */}
      {hasRole("ADMIN") && (
        <AdminDashboard userEmail={userEmail} roles={user.roles} enabled />
      )}
    </div>
  );
}

export default App;
