package com.expenseassistant.dto;

import com.expenseassistant.entity.Category;

import java.math.BigDecimal;

public record CategorySummaryResponse(Category category, BigDecimal amount) {
}
