package com.expenseassistant.dto;

import com.expenseassistant.entity.Category;

import java.math.BigDecimal;

/** status is SAFE, WARNING or EXCEEDED. */
public record BudgetStatusResponse(
        Category category,
        BigDecimal monthlyLimit,
        BigDecimal spent,
        BigDecimal remaining,
        BigDecimal percentageUsed,
        String status) {
}
