import { useEffect, useState } from "react";
import {
  fetchPendingExpenses,
  approveExpense,
  rejectExpense,
} from "../api/expenses";

export default function ManagerQueue({ userEmail, roles }) {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(false);
  const [busyId, setBusyId] = useState(null);
  const [toast, setToast] = useState(null); // {type: 'success'|'error', msg: string}

  const isAllowed = roles?.includes("MANAGER"); // 👈 only MANAGER, not ADMIN

  function showToast(type, msg) {
    setToast({ type, msg });
    setTimeout(() => setToast(null), 2000);
  }

  async function load() {
    if (!isAllowed) {
      setItems([]);
      setLoading(false);
      return;
    }

    setLoading(true);
    try {
      const data = await fetchPendingExpenses(userEmail, { roles });
      setItems(data);
    } catch (e) {
      if (e?.code === "ERR_CANCELED") return;
      console.error(e);
      const status = e?.response?.status;
      if (status === 401 || status === 403) {
        // silently swallow — no need to show scary errors
        setItems([]);
      } else {
        showToast("error", "Failed to load pending expenses");
      }
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    if (isAllowed) load();
    else {
      setItems([]);
      setLoading(false);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [userEmail, roles]);

  async function onApprove(id) {
    if (busyId) return;
    setBusyId(id);
    const prev = items;
    setItems(prev.filter((x) => x.id !== id));

    try {
      await approveExpense(userEmail, id, { roles });
      showToast("success", `Expense #${id} approved`);
    } catch (e) {
      console.error(e);
      const status = e?.response?.status;
      if (status === 409 || status === 404) {
        showToast("error", `Expense #${id} already processed. Refreshing…`);
        await load();
      } else {
        setItems(prev);
        showToast("error", "Approve failed — restoring item");
      }
    } finally {
      setBusyId(null);
    }
  }

  async function onReject(id) {
    if (busyId) return;
    if (!window.confirm(`Reject expense #${id}?`)) return;

    setBusyId(id);
    const prev = items;
    setItems(prev.filter((x) => x.id !== id));

    try {
      await rejectExpense(userEmail, id, { roles });
      showToast("success", `Expense #${id} rejected`);
    } catch (e) {
      console.error(e);
      const status = e?.response?.status;
      if (status === 409 || status === 404) {
        showToast("error", `Expense #${id} already processed. Refreshing…`);
        await load();
      } else {
        setItems(prev);
        showToast("error", "Reject failed — restoring item");
      }
    } finally {
      setBusyId(null);
    }
  }

  if (!isAllowed) return null;

  return (
    <div style={{ marginTop: 24 }}>
      <h2>Pending Approvals</h2>
      <button onClick={load} style={{ margin: "8px 0 16px" }}>Refresh</button>

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
          style={{ borderCollapse: "collapse", width: "100%", maxWidth: 1000 }}
        >
          <thead>
            <tr>
              <th>ID</th>
              <th>Title</th>
              <th>Amount</th>
              <th>Category</th>
              <th>Status</th>
              <th>Receipt</th> {/* 👈 new column */}
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {items.map((e) => (
              <tr key={e.id}>
                <td>{e.id}</td>
                <td>{e.title}</td>
                <td>{e.amount}</td>
                <td>{e.category}</td>
                <td>{e.status}</td>
                <td>
                  {e.receiptUrl ? (
                    <a 
  href={`http://localhost:8080${e.receiptUrl}`} 
  target="_blank" 
  rel="noreferrer"
>
  View
</a>
                  ) : (
                    "—"
                  )}
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
