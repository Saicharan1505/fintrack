import { useState } from "react";
import { createExpense, uploadReceipt } from "../api/expenses";
import { useUser } from "../contexts/UserContext";

export default function ExpenseForm() {
  const { userEmail } = useUser();               // we only need the email
  const [title, setTitle] = useState("");
  const [amount, setAmount] = useState("");
  const [category, setCategory] = useState("TRAVEL");
  const [notes, setNotes] = useState("");
  const [file, setFile] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    if (submitting) return;
    setSubmitting(true);

    try {
      // 1) Upload file (if any) to /api/expenses/upload → returns /uploads/<file>
      let receiptUrl = null;
      if (file) {
        receiptUrl = await uploadReceipt(file);
      }

      // 2) Create expense with notes + receiptUrl
      await createExpense(userEmail, {
        title: title.trim(),
        amount: Number(amount),
        category,
        notes: notes?.trim() || null,
        receiptUrl,
      });

      // 3) Reset form
      setTitle("");
      setAmount("");
      setCategory("TRAVEL");
      setNotes("");
      setFile(null);
      alert("Expense submitted!");
    } catch (err) {
      console.error(err);
      alert("Failed to submit expense. Check console for details.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} style={{ marginBottom: 20, textAlign: "left" }}>
      <div style={{ display: "flex", gap: 8, flexWrap: "wrap", alignItems: "center" }}>
        <input
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          placeholder="Title"
          required
          style={{ padding: 6, minWidth: 220 }}
        />
        <input
          type="number"
          step="0.01"
          value={amount}
          onChange={(e) => setAmount(e.target.value)}
          placeholder="Amount"
          required
          style={{ padding: 6, width: 120 }}
        />
        <select
          value={category}
          onChange={(e) => setCategory(e.target.value)}
          style={{ padding: 6 }}
        >
          <option value="TRAVEL">Travel</option>
          <option value="MEALS">Meals</option>
          <option value="LODGING">Lodging</option>
          <option value="OFFICE">Office Supplies</option>
          <option value="OTHER">Other</option>
        </select>
        <button type="submit" disabled={submitting} style={{ padding: "6px 12px" }}>
          {submitting ? "Submitting…" : "Submit"}
        </button>
      </div>

      <div style={{ marginTop: 10 }}>
        <textarea
          value={notes}
          onChange={(e) => setNotes(e.target.value)}
          placeholder="Notes (optional)"
          rows={3}
          style={{ width: 480, maxWidth: "100%", padding: 6 }}
        />
      </div>

      <div style={{ marginTop: 8 }}>
        <input
          type="file"
          accept="image/*,.pdf"
          onChange={(e) => setFile(e.target.files?.[0] ?? null)}
        />
        {file && (
          <div style={{ fontSize: 12, color: "#555", marginTop: 4 }}>
            Selected: {file.name}
          </div>
        )}
      </div>
    </form>
  );
}
