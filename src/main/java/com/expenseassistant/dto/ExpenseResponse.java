package com.expenseassistant.dto;

import com.expenseassistant.entity.Category;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ExpenseResponse(
        Long id,
        String merchantName,
        LocalDate expenseDate,
        BigDecimal amount,
        Category category,
        String description,
        List<String> items,
        String receiptFileName,
        LocalDateTime createdAt) {
}
