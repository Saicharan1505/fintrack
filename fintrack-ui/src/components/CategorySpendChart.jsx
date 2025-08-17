import {
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  Tooltip,
  Legend,
} from "recharts";

// Recharts wants an array like [{ name: 'TRAVEL', value: 123.45 }, ...]
function toChartData(spendByCategory = []) {
  return spendByCategory.map((c) => ({
    name: c.category,
    value: Number(c.total || 0),
  }));
}

export default function CategorySpendChart({ data }) {
  const chartData = toChartData(data);

  // simple color set; recharts will cycle through these
  const COLORS = ["#6366F1", "#10B981", "#F59E0B", "#EF4444", "#3B82F6", "#14B8A6"];

  if (!chartData.length) return <div style={{ color: "#666" }}>No data yet.</div>;

  return (
    <div className="card" style={{ height: 320 }}>
      <div className="card-title">Spend by Category</div>
      <ResponsiveContainer width="100%" height="85%">
        <PieChart>
          <Pie
            dataKey="value"
            nameKey="name"
            data={chartData}
            cx="50%"
            cy="50%"
            outerRadius="80%"
            isAnimationActive={false}
          >
            {chartData.map((_, i) => (
              <Cell key={i} fill={COLORS[i % COLORS.length]} />
            ))}
          </Pie>
          <Tooltip formatter={(v) => v.toLocaleString(undefined, { style: "currency", currency: "USD" })} />
          <Legend />
        </PieChart>
      </ResponsiveContainer>
    </div>
  );
}
