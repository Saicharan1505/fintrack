package com.fintrack.admin.dto;

import java.math.BigDecimal;
import java.util.List;

public record AdminOverview(
        long pendingCount,
        long approvedCount,
        long rejectedCount,
        BigDecimal approvedTotal,
        List<SpendByCategory> spendByCategory,
        List<ExpenseRow> recent) {
}
