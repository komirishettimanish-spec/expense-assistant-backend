package com.expenseassistant.dto;

import com.expenseassistant.entity.Category;

import java.math.BigDecimal;

public record BudgetResponse(Long id, Category category, BigDecimal monthlyLimit, Integer month, Integer year) {
}
