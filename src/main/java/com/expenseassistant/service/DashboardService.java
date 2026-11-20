package com.expenseassistant.service;

import com.expenseassistant.dto.DashboardResponse;
import com.expenseassistant.entity.Expense;
import com.expenseassistant.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ExpenseService expenseService;
    private final BudgetService budgetService;
    private final ForecastService forecastService;

    public DashboardResponse build(User user) {
        List<Expense> all = expenseService.findAll(user.getId());
        YearMonth month = YearMonth.now();
        List<Expense> thisMonth = all.stream()
                .filter(e -> YearMonth.from(e.getExpenseDate()).equals(month))
                .toList();

        return new DashboardResponse(
                ExpenseService.total(all),
                ExpenseService.total(thisMonth),
                all.size(),
                ExpenseService.summarize(thisMonth),
                ExpenseService.summarize(all),
                all.stream().limit(5).map(ExpenseService::toResponse).toList(),
                budgetService.status(user, month.getMonthValue(), month.getYear()),
                forecastService.forecast(user));
    }
}
