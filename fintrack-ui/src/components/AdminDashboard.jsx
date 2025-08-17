// import { useEffect, useState } from "react";
// import { fetchAdminOverview } from "../api/admin";

// export default function AdminDashboard({ userEmail, enabled = false }) {
//   const [data, setData] = useState(null);
//   const [err, setErr] = useState(null);
//   const [loading, setLoading] = useState(false);

//   useEffect(() => {
//     if (!enabled) {
//       // When disabled (non-admin), ensure no stale state or requests
//       setLoading(false);
//       setErr(null);
//       setData(null);
//       return;
//     }

//     const abort = new AbortController();

//     async function load() {
//       try {
//         setLoading(true);
//         setErr(null);
//         const d = await fetchAdminOverview(userEmail, { signal: abort.signal });
//         setData(d);
//       } catch (e) {
//         if (abort.signal.aborted) return;
//         console.error(e);
//         setErr("Failed to load admin overview");
//       } finally {
//         if (!abort.signal.aborted) setLoading(false);
//       }
//     }

//     load();
//     return () => abort.abort();
//   }, [userEmail, enabled]);

//   if (!enabled) return null; // do not render anything for non-admin
//   if (loading) return <p>Loading admin overview…</p>;
//   if (err) return <p style={{ color: "red" }}>{err}</p>;
//   if (!data) return null;

//   return (
//     <div>
//       {/* KPI cards */}
//       <div
//         style={{
//           display: "grid",
//           gridTemplateColumns: "repeat(4, 1fr)",
//           gap: 12,
//           margin: "12px 0",
//         }}
//       >
//         <Kpi label="Pending" value={data.pendingCount} />
//         <Kpi label="Approved" value={data.approvedCount} />
//         <Kpi label="Rejected" value={data.rejectedCount} />
//         <Kpi label="Approved Total" value={formatMoney(data.approvedTotal)} />
//       </div>

//       <div style={{ display: "grid", gridTemplateColumns: "1fr 2fr", gap: 16 }}>
//         {/* Spend by Category */}
//         <div>
//           <h3>Spend by Category (Approved)</h3>
//           {data.spendByCategory.length === 0 ? (
//             <p>No approved spend yet.</p>
//           ) : (
//             <table
//               border="1"
//               cellPadding="8"
//               style={{ borderCollapse: "collapse", width: "100%" }}
//             >
//               <thead>
//                 <tr>
//                   <th style={{ textAlign: "left" }}>Category</th>
//                   <th style={{ textAlign: "right" }}>Total</th>
//                 </tr>
//               </thead>
//               <tbody>
//                 {data.spendByCategory.map((c) => (
//                   <tr key={c.category}>
//                     <td>{c.category}</td>
//                     <td style={{ textAlign: "right" }}>
//                       {formatMoney(c.total)}
//                     </td>
//                   </tr>
//                 ))}
//               </tbody>
//             </table>
//           )}
//         </div>

//         {/* Recent expenses */}
//         <div>
//           <h3>Recent Expenses</h3>
//           {data.recent.length === 0 ? (
//             <p>No expenses yet.</p>
//           ) : (
//             <table
//               border="1"
//               cellPadding="8"
//               style={{ borderCollapse: "collapse", width: "100%" }}
//             >
//               <thead>
//                 <tr>
//                   <th style={{ textAlign: "left" }}>#</th>
//                   <th style={{ textAlign: "left" }}>Title</th>
//                   <th style={{ textAlign: "left" }}>Employee</th>
//                   <th style={{ textAlign: "left" }}>Status</th>
//                   <th style={{ textAlign: "right" }}>Amount</th>
//                 </tr>
//               </thead>
//               <tbody>
//                 {data.recent.map((e) => (
//                   <tr key={e.id}>
//                     <td>#{e.id}</td>
//                     <td>{e.title}</td>
//                     <td>{e.employeeName}</td>
//                     <td>
//                       <Badge text={e.status} />
//                     </td>
//                     <td style={{ textAlign: "right" }}>
//                       {formatMoney(e.amount)}
//                     </td>
//                   </tr>
//                 ))}
//               </tbody>
//             </table>
//           )}
//         </div>
//       </div>
//     </div>
//   );
// }

// function Kpi({ label, value }) {
//   return (
//     <div style={{ padding: 12, border: "1px solid #eee", borderRadius: 10 }}>
//       <div style={{ color: "#666", fontSize: 13 }}>{label}</div>
//       <div style={{ fontSize: 22, fontWeight: 700 }}>{value}</div>
//     </div>
//   );
// }

// function Badge({ text }) {
//   const styles =
//     {
//       PENDING: { bg: "#fff7e6", bd: "#ffe1a8", fg: "#8a5a00" },
//       APPROVED: { bg: "#e6ffed", bd: "#b7f0c2", fg: "#045d1a" },
//       REJECTED: { bg: "#ffecec", bd: "#ffc2c2", fg: "#7a0b0b" },
//     }[text] || { bg: "#f2f2f2", bd: "#ddd", fg: "#333" };

//   return (
//     <span
//       style={{
//         padding: "2px 8px",
//         borderRadius: 12,
//         fontSize: 12,
//         background: styles.bg,
//         border: `1px solid ${styles.bd}`,
//         color: styles.fg,
//       }}
//     >
//       {text}
//     </span>
//   );
// }

// function formatMoney(x) {
//   // keep it simple; you can localize later
//   return Number(x).toLocaleString(undefined, {
//     style: "currency",
//     currency: "USD",
//   });
// }


import { useEffect, useState } from "react";
import { fetchAdminOverview } from "../api/admin";
import CategorySpendChart from "./CategorySpendChart";


export default function AdminDashboard({ userEmail, enabled = false }) {
  const [data, setData] = useState(null);
  const [err, setErr] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!enabled) {
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
        const d = await fetchAdminOverview(userEmail, { signal: abort.signal });
        setData(d);
      } catch (e) {
        if (abort.signal.aborted) return;
        console.error(e);
        setErr("Failed to load admin overview");
      } finally {
        if (!abort.signal.aborted) setLoading(false);
      }
    }

    load();
    return () => abort.abort();
  }, [userEmail, enabled]);

  if (!enabled) return null;
  if (loading) return <p>Loading admin overview…</p>;
  if (err) return <p style={{ color: "red" }}>{err}</p>;
  if (!data) return null;

  return (
    <div style={{ marginTop: 24, textAlign: "left" }}>
      {/* KPI cards */}
      <div className="grid-4">
        <Kpi label="Pending" value={data.pendingCount} />
        <Kpi label="Approved" value={data.approvedCount} />
        <Kpi label="Rejected" value={data.rejectedCount} />
        <Kpi label="Approved Total" value={formatMoney(data.approvedTotal)} emphasized />
      </div>

      {/* Chart + Recent table */}
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
