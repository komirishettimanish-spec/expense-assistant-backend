package com.expenseassistant.dto;

import java.math.BigDecimal;

public record SpendingForecastResponse(
        BigDecimal currentSpending,
        int daysElapsed,
        BigDecimal averageDailySpending,
        BigDecimal projectedMonthEndSpending,
        int remainingDays) {
}
