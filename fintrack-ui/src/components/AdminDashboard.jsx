import { useEffect, useState } from "react";
import { fetchAdminOverview } from "../api/admin";
import CategorySpendChart from "./CategorySpendChart";

export default function AdminDashboard({ userEmail, roles, enabled = false }) {
  const [data, setData] = useState(null);
  const [err, setErr] = useState(null);
  const [loading, setLoading] = useState(false);

  const isAllowed = roles?.includes("ADMIN");

  useEffect(() => {
    if (!enabled || !isAllowed) {
      setLoading(false);
      setErr(null);
      setData(null);
      return;
    }

    const abort = new AbortController();

    async function load() {
      try {
        setLoading(true);
        setErr(null);
        const d = await fetchAdminOverview(userEmail, { signal: abort.signal, roles });
        setData(d);
      } catch (e) {
        if (abort.signal.aborted) return;
        const status = e?.response?.status;
        if (status === 401 || status === 403) {
          // silently ignore unauthorized
          setData(null);
        } else {
          console.error(e);
          setErr("Failed to load admin overview");
        }
      } finally {
        if (!abort.signal.aborted) setLoading(false);
      }
    }

    load();
    return () => abort.abort();
  }, [userEmail, enabled, isAllowed, roles]);

  if (!enabled || !isAllowed) return null;
  if (loading) return <p>Loading admin overview…</p>;
  if (err) return <p style={{ color: "red" }}>{err}</p>;
  if (!data) return null;

  return (
    <div style={{ marginTop: 24, textAlign: "left" }}>
      <div className="grid-4">
        <Kpi label="Pending" value={data.pendingCount} />
        <Kpi label="Approved" value={data.approvedCount} />
        <Kpi label="Rejected" value={data.rejectedCount} />
        <Kpi label="Approved Total" value={formatMoney(data.approvedTotal)} emphasized />
      </div>

      <div className="grid-2" style={{ marginTop: 16 }}>
        <CategorySpendChart data={data.spendByCategory} />
        <div className="card">
          <div className="card-title">Recent Expenses</div>
          {data.recent.length === 0 ? (
            <p className="muted">No expenses yet.</p>
          ) : (
            <div className="table-wrap">
              <table className="tbl">
                <thead>
                  <tr>
                    <th>#</th>
                    <th>Title</th>
                    <th>Employee</th>
                    <th>Status</th>
                    <th className="tr">Amount</th>
                  </tr>
                </thead>
                <tbody>
                  {data.recent.map((e) => (
                    <tr key={e.id}>
                      <td>#{e.id}</td>
                      <td>{e.title}</td>
                      <td>{e.employeeName}</td>
                      <td><Badge text={e.status} /></td>
                      <td className="tr">{formatMoney(e.amount)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

function Kpi({ label, value, emphasized = false }) {
  return (
    <div className="card">
      <div className="muted">{label}</div>
      <div style={{ fontSize: emphasized ? 24 : 20, fontWeight: 700 }}>{value}</div>
    </div>
  );
}

function Badge({ text }) {
  const styles =
    {
      PENDING: { bg: "#fff7e6", bd: "#ffe1a8", fg: "#8a5a00" },
      APPROVED: { bg: "#e6ffed", bd: "#b7f0c2", fg: "#045d1a" },
      REJECTED: { bg: "#ffecec", bd: "#ffc2c2", fg: "#7a0b0b" },
    }[text] || { bg: "#f2f2f2", bd: "#ddd", fg: "#333" };

  return (
    <span
      style={{
        padding: "2px 8px",
        borderRadius: 12,
        fontSize: 12,
        background: styles.bg,
        border: `1px solid ${styles.bd}`,
        color: styles.fg,
      }}
    >
      {text}
    </span>
  );
}

function formatMoney(x) {
  return Number(x).toLocaleString(undefined, {
    style: "currency",
    currency: "USD",
  });
}
