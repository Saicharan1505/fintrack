package com.fintrack.admin.dto;

import java.math.BigDecimal;

public record SpendByCategory(String category, BigDecimal total) {
}
