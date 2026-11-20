package com.expenseassistant.service;

import com.expenseassistant.dto.CategorySummaryResponse;
import com.expenseassistant.dto.ForecastInsightResponse;
import com.expenseassistant.dto.SpendingForecastResponse;
import com.expenseassistant.entity.Expense;
import com.expenseassistant.entity.User;
import com.expenseassistant.ai.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ForecastService {

    private final ExpenseService expenseService;
    private final GeminiService geminiService;

    /** projected = spent so far / days elapsed * days in month. Plain arithmetic, no machine learning. */
    public SpendingForecastResponse forecast(User user) {
        LocalDate today = LocalDate.now();
        YearMonth month = YearMonth.from(today);
        BigDecimal current = ExpenseService.total(
                expenseService.findInRange(user.getId(), month.atDay(1), month.atEndOfMonth()));

        int daysElapsed = today.getDayOfMonth();
        int daysInMonth = month.lengthOfMonth();
        BigDecimal elapsed = BigDecimal.valueOf(daysElapsed);

        BigDecimal average = current.divide(elapsed, 2, RoundingMode.HALF_UP);
        BigDecimal projected = current.multiply(BigDecimal.valueOf(daysInMonth))
                .divide(elapsed, 2, RoundingMode.HALF_UP);

        return new SpendingForecastResponse(current, daysElapsed, average, projected, daysInMonth - daysElapsed);
    }

    /** Needs Gemini; the numeric forecast above never does. */
    public ForecastInsightResponse insight(User user) {
        SpendingForecastResponse forecast = forecast(user);
        YearMonth month = YearMonth.now();
        List<Expense> expenses = expenseService.findInRange(user.getId(), month.atDay(1), month.atEndOfMonth());

        StringBuilder summary = new StringBuilder();
        summary.append("Spent so far this month: INR ").append(forecast.currentSpending().toPlainString()).append('\n');
        summary.append("Days elapsed: ").append(forecast.daysElapsed())
                .append(", days remaining: ").append(forecast.remainingDays()).append('\n');
        summary.append("Average daily spending: INR ").append(forecast.averageDailySpending().toPlainString()).append('\n');
        summary.append("Projected month-end spending: INR ").append(forecast.projectedMonthEndSpending().toPlainString()).append('\n');
        summary.append("Spending by category:\n");
        for (CategorySummaryResponse c : ExpenseService.summarize(expenses)) {
            summary.append("- ").append(c.category()).append(": INR ").append(c.amount().toPlainString()).append('\n');
        }

        String insight = geminiService.explainForecast(summary.toString());
        return new ForecastInsightResponse(insight.strip(), forecast);
    }
}
