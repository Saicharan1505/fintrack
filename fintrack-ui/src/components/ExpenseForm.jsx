import { useState } from "react";
import api from "../api/client";

export default function ExpenseForm() {
  const [title, setTitle] = useState("");
  const [amount, setAmount] = useState("");
  const [category, setCategory] = useState("TRAVEL");

  const submitExpense = async (e) => {
    e.preventDefault();
    await api.post("/expenses", 
      { title, amount, category }, 
      { headers: { "X-User-Email": "evan.employee@demo.local" } }
    );
    setTitle("");
    setAmount("");
    setCategory("TRAVEL");
    alert("Expense submitted!");
  };

  return (
    <form onSubmit={submitExpense}>
      <input value={title} onChange={e => setTitle(e.target.value)} placeholder="Title" required />
      <input type="number" value={amount} onChange={e => setAmount(e.target.value)} placeholder="Amount" required />
      <select value={category} onChange={e => setCategory(e.target.value)}>
        <option value="TRAVEL">Travel</option>
        <option value="MEALS">Meals</option>
        <option value="SUPPLIES">Supplies</option>
      </select>
      <button type="submit">Submit</button>
    </form>
  );
}
