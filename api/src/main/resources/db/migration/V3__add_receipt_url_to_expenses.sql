-- Adds the column used by Expense.receiptUrl
ALTER TABLE expenses
ADD COLUMN IF NOT EXISTS receipt_url VARCHAR(512);
