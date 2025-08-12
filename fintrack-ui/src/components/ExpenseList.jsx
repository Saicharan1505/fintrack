import { useEffect, useState } from "react";
import api from "../api/client";

export default function ExpenseList() {
  const [expenses, setExpenses] = useState([]);

  useEffect(() => {
    api.get("/expenses/mine", {
      headers: { "X-User-Email": "evan.employee@demo.local" }
    }).then(res => setExpenses(res.data));
  }, []);

  return (
    <div>
      <h2>My Expenses</h2>
      <ul>
        {expenses.map(e => (
          <li key={e.id}>
            {e.title} — ${e.amount} — {e.status}
          </li>
        ))}
      </ul>
    </div>
  );
}
