package com.expenseassistant.service;

import com.expenseassistant.dto.CategorySummaryResponse;
import com.expenseassistant.dto.ExpenseRequest;
import com.expenseassistant.dto.ExpenseResponse;
import com.expenseassistant.entity.Category;
import com.expenseassistant.entity.Expense;
import com.expenseassistant.entity.User;
import com.expenseassistant.exception.BadRequestException;
import com.expenseassistant.exception.ResourceNotFoundException;
import com.expenseassistant.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public ExpenseResponse create(User user, ExpenseRequest request) {
        Expense expense = new Expense();
        expense.setUser(user);
        apply(expense, request);
        return toResponse(expenseRepository.save(expense));
    }

    public List<ExpenseResponse> list(User user, String category, LocalDate fromDate, LocalDate toDate) {
        Category filter = null;
        if (category != null && !category.isBlank()) {
            filter = Category.parse(category);
            if (filter == null) {
                throw new BadRequestException("Unknown category: " + category);
            }
        }
        final Category categoryFilter = filter;
        return findAll(user.getId()).stream()
                .filter(e -> categoryFilter == null || e.getCategory() == categoryFilter)
                .filter(e -> fromDate == null || !e.getExpenseDate().isBefore(fromDate))
                .filter(e -> toDate == null || !e.getExpenseDate().isAfter(toDate))
                .map(ExpenseService::toResponse)
                .toList();
    }

    public ExpenseResponse get(User user, Long id) {
        return toResponse(find(user, id));
    }

    public ExpenseResponse update(User user, Long id, ExpenseRequest request) {
        Expense expense = find(user, id);
        apply(expense, request);
        return toResponse(expenseRepository.save(expense));
    }

    public void delete(User user, Long id) {
        expenseRepository.delete(find(user, id));
    }

    public List<Expense> findAll(Long userId) {
        return expenseRepository.findByUserIdOrderByExpenseDateDescIdDesc(userId);
    }

    public List<Expense> findInRange(Long userId, LocalDate from, LocalDate to) {
        return expenseRepository.findByUserIdAndExpenseDateBetween(userId, from, to);
    }

    public static BigDecimal total(List<Expense> expenses) {
        return expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public static Map<Category, BigDecimal> totalsByCategory(List<Expense> expenses) {
        Map<Category, BigDecimal> totals = new EnumMap<>(Category.class);
        for (Expense e : expenses) {
            totals.merge(e.getCategory(), e.getAmount(), BigDecimal::add);
        }
        return totals;
    }

    public static List<CategorySummaryResponse> summarize(List<Expense> expenses) {
        return totalsByCategory(expenses).entrySet().stream()
                .map(entry -> new CategorySummaryResponse(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(CategorySummaryResponse::amount).reversed())
                .toList();
    }

    public static ExpenseResponse toResponse(Expense e) {
        List<String> items = (e.getItems() == null || e.getItems().isBlank())
                ? List.of()
                : Arrays.asList(e.getItems().split("\n"));
        return new ExpenseResponse(e.getId(), e.getMerchantName(), e.getExpenseDate(), e.getAmount(),
                e.getCategory(), e.getDescription(), items, e.getReceiptFileName(), e.getCreatedAt());
    }

    private Expense find(User user, Long id) {
        return expenseRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
    }

    private void apply(Expense expense, ExpenseRequest r) {
        expense.setMerchantName(r.merchantName().trim());
        expense.setExpenseDate(r.expenseDate());
        expense.setAmount(r.amount().setScale(2, RoundingMode.HALF_UP));
        expense.setCategory(r.category());
        expense.setDescription(r.description() == null || r.description().isBlank() ? null : r.description().trim());
        expense.setReceiptFileName(r.receiptFileName());
        if (r.items() == null || r.items().isEmpty()) {
            expense.setItems(null);
        } else {
            expense.setItems(String.join("\n", r.items().stream()
                    .filter(i -> i != null && !i.isBlank())
                    .map(i -> i.replace("\n", " ").trim())
                    .toList()));
        }
    }
}
