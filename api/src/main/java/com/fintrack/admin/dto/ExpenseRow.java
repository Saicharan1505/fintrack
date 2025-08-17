package com.fintrack.admin.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ExpenseRow(
        Integer id,
        String title,
        BigDecimal amount,
        String category,
        String status,
        String employeeName,
        OffsetDateTime createdAt) {
}
