package com.fintrack.expense.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateExpenseRequest(
                @NotBlank String title,
                @NotNull @DecimalMin("0.01") BigDecimal amount,
                @NotBlank String category,
                String notes,
                String receiptUrl) {
}
