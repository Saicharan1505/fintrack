import { useEffect, useState } from "react";
import { fetchMyExpenses } from "../api/expenses";
import { useUser } from "../contexts/UserContext";

export default function ExpenseList() {
  const { userEmail, getUserRoles } = useUser();
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!userEmail) return;
    const abort = new AbortController();

    async function load() {
      setLoading(true);
      try {
        const data = await fetchMyExpenses(userEmail, {
          roles: getUserRoles?.(),
          signal: abort.signal,
        });
        setItems(Array.isArray(data) ? data : []);
      } catch (e) {
        if (e?.code !== "ERR_CANCELED") {
          console.error("Failed to load my expenses:", e);
        }
      } finally {
        if (!abort.signal.aborted) setLoading(false);
      }
    }

    load();
    return () => abort.abort();
  }, [userEmail, getUserRoles]);

  return (
    <div style={{ marginTop: 16 }}>
      <h2>My Expenses</h2>

      {loading ? (
        <p>Loading…</p>
      ) : items.length === 0 ? (
        <p>No expenses yet.</p>
      ) : (
        <table
          border="1"
          cellPadding="8"
          style={{ borderCollapse: "collapse", width: "100%", maxWidth: 900 }}
        >
          <thead>
            <tr>
              <th style={{ textAlign: "left" }}>Title</th>
              <th style={{ textAlign: "left" }}>Amount</th>
              <th style={{ textAlign: "left" }}>Category</th>
              <th style={{ textAlign: "left" }}>Status</th>
              <th style={{ textAlign: "left" }}>Notes</th>
              <th style={{ textAlign: "left" }}>Receipt</th>
            </tr>
          </thead>
          <tbody>
            {items.map((e) => (
              <tr key={e.id}>
                <td>{e.title}</td>
                <td>{e.amount}</td>
                <td>{e.category}</td>
                <td>
                  <span
                    style={{
                      padding: "2px 8px",
                      borderRadius: 12,
                      fontSize: 12,
                      background:
                        e.status === "APPROVED"
                          ? "#e6ffed"
                          : e.status === "REJECTED"
                          ? "#ffecec"
                          : "#fff7e6",
                      border:
                        e.status === "APPROVED"
                          ? "1px solid #b7f0c2"
                          : e.status === "REJECTED"
                          ? "1px solid #ffc2c2"
                          : "1px solid #ffe1a8",
                      color:
                        e.status === "APPROVED"
                          ? "#045d1a"
                          : e.status === "REJECTED"
                          ? "#7a0b0b"
                          : "#8a5a00",
                    }}
                  >
                    {e.status}
                  </span>
                </td>
                <td style={{ maxWidth: 320, whiteSpace: "pre-wrap" }}>
                  {e.notes ?? "—"}
                </td>
                <td>
                  {e.receiptUrl ? (
                    <a href={e.receiptUrl} target="_blank" rel="noreferrer">
                      View
                    </a>
                  ) : (
                    "—"
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
