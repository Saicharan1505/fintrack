import { useEffect, useState } from "react";
import {
  fetchPendingExpenses,
  approveExpense,
  rejectExpense,
} from "../api/expenses";

export default function ManagerQueue({ userEmail, roles }) {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [busyId, setBusyId] = useState(null);
  const [toast, setToast] = useState(null); // {type: 'success'|'error', msg: string}

  function showToast(type, msg) {
    setToast({ type, msg });
    setTimeout(() => setToast(null), 2000);
  }

  async function load() {
    if (!roles?.includes("MANAGER") && !roles?.includes("ADMIN")) {
      setItems([]);
      setLoading(false);
      return;
    }

    setLoading(true);
    try {
      const data = await fetchPendingExpenses(userEmail);
      setItems(data);
    } catch (e) {
      console.error(e);
      const status = e?.response?.status;
      if (status === 401 || status === 403) {
        showToast("error", "Not authorized to view pending expenses");
      } else {
        showToast("error", "Failed to load pending expenses");
      }
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    if (userEmail) load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [userEmail, roles]);

  async function onApprove(id) {
    if (busyId) return;
    setBusyId(id);

    // optimistic remove
    const prev = items;
    setItems(prev.filter((x) => x.id !== id));

    try {
      await approveExpense(userEmail, id);
      showToast("success", `Expense #${id} approved`);
    } catch (e) {
      console.error(e);
      const status = e?.response?.status;
      if (status === 409) {
        showToast("error", `Expense #${id} was already processed. Refreshing…`);
        await load(); // don’t rollback
      } else if (status === 404) {
        showToast("error", `Expense #${id} no longer exists. Refreshing…`);
        await load(); // don’t rollback
      } else if (status === 401 || status === 403) {
        showToast("error", `Not authorized to approve`);
        setItems(prev); // rollback
      } else {
        showToast("error", "Approve failed — restoring item");
        setItems(prev); // rollback
      }
    } finally {
      setBusyId(null);
    }
  }

  async function onReject(id) {
    if (busyId) return;
    const ok = window.confirm(`Reject expense #${id}?`);
    if (!ok) return;

    setBusyId(id);

    // optimistic remove
    const prev = items;
    setItems(prev.filter((x) => x.id !== id));

    try {
      await rejectExpense(userEmail, id);
      showToast("success", `Expense #${id} rejected`);
    } catch (e) {
      console.error(e);
      const status = e?.response?.status;
      if (status === 409) {
        showToast("error", `Expense #${id} was already processed. Refreshing…`);
        await load();
      } else if (status === 404) {
        showToast("error", `Expense #${id} no longer exists. Refreshing…`);
        await load();
      } else if (status === 401 || status === 403) {
        showToast("error", `Not authorized to reject`);
        setItems(prev); // rollback
      } else {
        showToast("error", "Reject failed — restoring item");
        setItems(prev); // rollback
      }
    } finally {
      setBusyId(null);
    }
  }

  // 🚨 Don’t render for plain employees
  if (!roles?.includes("MANAGER") && !roles?.includes("ADMIN")) {
    return null;
  }

  return (
    <div style={{ marginTop: 24 }}>
      <h2>Pending Approvals</h2>
      <button onClick={load} style={{ margin: "8px 0 16px" }}>Refresh</button>

      {/* Tiny toast */}
      {toast && (
        <div
          style={{
            margin: "8px 0",
            padding: "8px 12px",
            borderRadius: 6,
            display: "inline-block",
            background: toast.type === "success" ? "#e6ffed" : "#ffecec",
            color: toast.type === "success" ? "#045d1a" : "#7a0b0b",
            border:
              toast.type === "success" ? "1px solid #b7f0c2" : "1px solid #ffc2c2",
          }}
        >
          {toast.msg}
        </div>
      )}

      {loading ? (
        <p>Loading…</p>
      ) : items.length === 0 ? (
        <p>No pending expenses 🎉</p>
      ) : (
        <table
          border="1"
          cellPadding="8"
          style={{ borderCollapse: "collapse", width: "100%", maxWidth: 900 }}
        >
          <thead>
            <tr>
              <th style={{ textAlign: "left" }}>ID</th>
              <th style={{ textAlign: "left" }}>Title</th>
              <th style={{ textAlign: "left" }}>Amount</th>
              <th style={{ textAlign: "left" }}>Category</th>
              <th style={{ textAlign: "left" }}>Status</th>
              <th style={{ textAlign: "left" }}>Actions</th>
            </tr>
          </thead>
          <tbody>
            {items.map((e) => (
              <tr key={e.id}>
                <td>#{e.id}</td>
                <td>{e.title}</td>
                <td>{e.amount}</td>
                <td>{e.category}</td>
                <td>
                  <span
                    style={{
                      padding: "2px 8px",
                      borderRadius: 12,
                      fontSize: 12,
                      background: "#fff7e6",
                      border: "1px solid #ffe1a8",
                      color: "#8a5a00",
                    }}
                  >
                    {e.status}
                  </span>
                </td>
                <td>
                  <button
                    onClick={() => onApprove(e.id)}
                    disabled={!!busyId}
                    style={{ marginRight: 8 }}
                  >
                    {busyId === e.id ? "Approving…" : "Approve"}
                  </button>
                  <button onClick={() => onReject(e.id)} disabled={!!busyId}>
                    {busyId === e.id ? "Rejecting…" : "Reject"}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
