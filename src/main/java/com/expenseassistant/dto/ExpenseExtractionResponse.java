package com.expenseassistant.dto;

import com.expenseassistant.entity.Category;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Result of OCR + Gemini. Any field may be null when it could not be read; the user reviews it before saving. */
public record ExpenseExtractionResponse(
        String merchantName,
        LocalDate expenseDate,
        BigDecimal amount,
        Category category,
        String description,
        List<String> items,
        String receiptFileName,
        String ocrText) {

    public ExpenseExtractionResponse withReceipt(String fileName, String text) {
        return new ExpenseExtractionResponse(merchantName, expenseDate, amount, category, description, items, fileName, text);
    }
}
