package com.expenseassistant.dto;

import com.expenseassistant.entity.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ExpenseRequest(
        @NotBlank(message = "Merchant name is required") @Size(max = 150) String merchantName,
        @NotNull(message = "Expense date is required (yyyy-MM-dd)") LocalDate expenseDate,
        @NotNull(message = "Amount is required") @Positive(message = "Amount must be greater than zero") BigDecimal amount,
        @NotNull(message = "Category is required and must be one of FOOD, TRAVEL, SHOPPING, BILLS, HEALTH, ENTERTAINMENT, EDUCATION, GROCERIES, OTHER") Category category,
        @Size(max = 500) String description,
        List<String> items,
        @Size(max = 255) String receiptFileName) {
}
