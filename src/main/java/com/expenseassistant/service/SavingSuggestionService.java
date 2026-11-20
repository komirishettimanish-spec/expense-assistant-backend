package com.expenseassistant.service;

import com.expenseassistant.ai.GeminiService;
import com.expenseassistant.dto.BudgetStatusResponse;
import com.expenseassistant.dto.CategorySummaryResponse;
import com.expenseassistant.dto.SavingSuggestionResponse;
import com.expenseassistant.dto.SpendingForecastResponse;
import com.expenseassistant.entity.Expense;
import com.expenseassistant.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SavingSuggestionService {

    private final ExpenseService expenseService;
    private final BudgetService budgetService;
    private final ForecastService forecastService;
    private final GeminiService geminiService;

    public SavingSuggestionResponse suggest(User user) {
        YearMonth month = YearMonth.now();
        List<Expense> thisMonth = expenseService.findInRange(user.getId(), month.atDay(1), month.atEndOfMonth());
        if (thisMonth.isEmpty()) {
            return new SavingSuggestionResponse(List.of(
                    "No expenses recorded this month yet. Add some expenses to get personalised saving suggestions."));
        }

        YearMonth previous = month.minusMonths(1);
        List<Expense> lastMonth = expenseService.findInRange(user.getId(), previous.atDay(1), previous.atEndOfMonth());
        SpendingForecastResponse forecast = forecastService.forecast(user);

        StringBuilder summary = new StringBuilder();
        summary.append("Total spent this month so far: INR ").append(ExpenseService.total(thisMonth).toPlainString()).append('\n');
        summary.append("Projected month-end spending: INR ").append(forecast.projectedMonthEndSpending().toPlainString()).append('\n');
        summary.append("This month by category:\n");
        for (CategorySummaryResponse c : ExpenseService.summarize(thisMonth)) {
            summary.append("- ").append(c.category()).append(": INR ").append(c.amount().toPlainString()).append('\n');
        }
        if (!lastMonth.isEmpty()) {
            summary.append("Last month total: INR ").append(ExpenseService.total(lastMonth).toPlainString()).append('\n');
            summary.append("Last month by category:\n");
            for (CategorySummaryResponse c : ExpenseService.summarize(lastMonth)) {
                summary.append("- ").append(c.category()).append(": INR ").append(c.amount().toPlainString()).append('\n');
            }
        }
        List<BudgetStatusResponse> budgets = budgetService.status(user, month.getMonthValue(), month.getYear());
        if (!budgets.isEmpty()) {
            summary.append("Budgets this month:\n");
            for (BudgetStatusResponse b : budgets) {
                summary.append("- ").append(b.category()).append(": limit INR ").append(b.monthlyLimit().toPlainString())
                        .append(", spent INR ").append(b.spent().toPlainString())
                        .append(" (").append(b.percentageUsed().toPlainString()).append("%, ").append(b.status()).append(")\n");
            }
        }

        return new SavingSuggestionResponse(geminiService.suggestSavings(summary.toString()));
    }
}
