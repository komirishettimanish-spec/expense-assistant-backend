package com.expenseassistant.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(
        BigDecimal totalExpenses,
        BigDecimal currentMonthExpenses,
        long expenseCount,
        List<CategorySummaryResponse> categorySummary,
        List<CategorySummaryResponse> allTimeCategorySummary,
        List<ExpenseResponse> recentExpenses,
        List<BudgetStatusResponse> budgetStatus,
        SpendingForecastResponse forecast) {
}
