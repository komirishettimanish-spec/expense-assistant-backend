package com.expenseassistant.dto;

import com.expenseassistant.entity.Category;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/** month and year are optional and default to the current month. */
public record BudgetRequest(
        @NotNull(message = "Category is required and must be one of FOOD, TRAVEL, SHOPPING, BILLS, HEALTH, ENTERTAINMENT, EDUCATION, GROCERIES, OTHER") Category category,
        @NotNull(message = "Monthly limit is required") @Positive(message = "Monthly limit must be greater than zero") BigDecimal monthlyLimit,
        @Min(value = 1, message = "Month must be 1-12") @Max(value = 12, message = "Month must be 1-12") Integer month,
        @Min(value = 2000, message = "Year is not valid") @Max(value = 2100, message = "Year is not valid") Integer year) {
}
