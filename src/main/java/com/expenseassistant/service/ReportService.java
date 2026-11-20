package com.expenseassistant.service;

import com.expenseassistant.dto.CategorySummaryResponse;
import com.expenseassistant.dto.MonthlySummaryResponse;
import com.expenseassistant.entity.Expense;
import com.expenseassistant.entity.User;
import com.expenseassistant.util.PeriodUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ExpenseService expenseService;
    private final BudgetService budgetService;

    public MonthlySummaryResponse monthly(User user, Integer month, Integer year) {
        YearMonth period = PeriodUtil.resolve(month, year);
        List<Expense> expenses = expenseService.findInRange(user.getId(), period.atDay(1), period.atEndOfMonth());
        List<CategorySummaryResponse> breakdown = ExpenseService.summarize(expenses);

        return new MonthlySummaryResponse(
                period.getMonthValue(),
                period.getYear(),
                ExpenseService.total(expenses),
                expenses.size(),
                breakdown.isEmpty() ? null : breakdown.get(0).category(),
                breakdown,
                budgetService.status(user, period.getMonthValue(), period.getYear()));
    }
}
