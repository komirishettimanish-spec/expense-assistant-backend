package com.expenseassistant.dto;

import com.expenseassistant.entity.Category;

import java.math.BigDecimal;
import java.util.List;

public record MonthlySummaryResponse(
        int month,
        int year,
        BigDecimal totalSpending,
        int expenseCount,
        Category highestSpendingCategory,
        List<CategorySummaryResponse> categoryBreakdown,
        List<BudgetStatusResponse> budgetStatus) {
}
