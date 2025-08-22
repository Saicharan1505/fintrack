package com.fintrack.expense.dto;

import java.math.BigDecimal;

public record ExpenseResponse(
                Integer id,
                String title,
                BigDecimal amount,
                String category,
                String status,
                String notes, // NEW
                String receiptUrl // NEW
) {
}
