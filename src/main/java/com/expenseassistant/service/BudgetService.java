package com.expenseassistant.service;

import com.expenseassistant.dto.BudgetRequest;
import com.expenseassistant.dto.BudgetResponse;
import com.expenseassistant.dto.BudgetStatusResponse;
import com.expenseassistant.entity.Budget;
import com.expenseassistant.entity.Category;
import com.expenseassistant.entity.Expense;
import com.expenseassistant.entity.User;
import com.expenseassistant.exception.BadRequestException;
import com.expenseassistant.exception.ResourceNotFoundException;
import com.expenseassistant.repository.BudgetRepository;
import com.expenseassistant.util.PeriodUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final ExpenseService expenseService;
    private final BigDecimal warningThreshold;

    public BudgetService(BudgetRepository budgetRepository,
                         ExpenseService expenseService,
                         @Value("${budget.warning-threshold:75}") int warningThreshold) {
        this.budgetRepository = budgetRepository;
        this.expenseService = expenseService;
        this.warningThreshold = BigDecimal.valueOf(warningThreshold);
    }

    public BudgetResponse create(User user, BudgetRequest request) {
        YearMonth period = PeriodUtil.resolve(request.month(), request.year());
        ensureNoDuplicate(user, request.category(), period, null);

        Budget budget = new Budget();
        budget.setUser(user);
        apply(budget, request, period);
        return toResponse(budgetRepository.save(budget));
    }

    public List<BudgetResponse> list(User user) {
        return budgetRepository.findByUserIdOrderByYearDescMonthDescCategoryAsc(user.getId()).stream()
                .map(BudgetService::toResponse)
                .toList();
    }

    public BudgetResponse update(User user, Long id, BudgetRequest request) {
        Budget budget = find(user, id);
        YearMonth period = PeriodUtil.resolve(request.month(), request.year());
        ensureNoDuplicate(user, request.category(), period, id);
        apply(budget, request, period);
        return toResponse(budgetRepository.save(budget));
    }

    public void delete(User user, Long id) {
        budgetRepository.delete(find(user, id));
    }

    public List<BudgetStatusResponse> status(User user, Integer month, Integer year) {
        YearMonth period = PeriodUtil.resolve(month, year);
        List<Budget> budgets = budgetRepository.findByUserIdAndMonthAndYear(
                user.getId(), period.getMonthValue(), period.getYear());
        if (budgets.isEmpty()) {
            return List.of();
        }
        List<Expense> expenses = expenseService.findInRange(user.getId(), period.atDay(1), period.atEndOfMonth());
        Map<Category, BigDecimal> spentByCategory = ExpenseService.totalsByCategory(expenses);
        return budgets.stream()
                .map(b -> toStatus(b, spentByCategory.getOrDefault(b.getCategory(), BigDecimal.ZERO)))
                .toList();
    }

    private BudgetStatusResponse toStatus(Budget budget, BigDecimal spent) {
        BigDecimal limit = budget.getMonthlyLimit();
        BigDecimal percentage = spent.multiply(BigDecimal.valueOf(100)).divide(limit, 1, RoundingMode.HALF_UP);
        String status;
        if (spent.compareTo(limit) >= 0) {
            status = "EXCEEDED";
        } else if (percentage.compareTo(warningThreshold) >= 0) {
            status = "WARNING";
        } else {
            status = "SAFE";
        }
        return new BudgetStatusResponse(budget.getCategory(), limit, spent.setScale(2, RoundingMode.HALF_UP),
                limit.subtract(spent).setScale(2, RoundingMode.HALF_UP), percentage, status);
    }

    private Budget find(User user, Long id) {
        return budgetRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
    }

    private void ensureNoDuplicate(User user, Category category, YearMonth period, Long currentId) {
        budgetRepository.findByUserIdAndCategoryAndMonthAndYear(
                        user.getId(), category, period.getMonthValue(), period.getYear())
                .filter(existing -> !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new BadRequestException("A budget for " + category + " already exists for "
                            + period.getMonthValue() + "/" + period.getYear());
                });
    }

    private void apply(Budget budget, BudgetRequest request, YearMonth period) {
        budget.setCategory(request.category());
        budget.setMonthlyLimit(request.monthlyLimit().setScale(2, RoundingMode.HALF_UP));
        budget.setMonth(period.getMonthValue());
        budget.setYear(period.getYear());
    }

    private static BudgetResponse toResponse(Budget b) {
        return new BudgetResponse(b.getId(), b.getCategory(), b.getMonthlyLimit(), b.getMonth(), b.getYear());
    }
}
