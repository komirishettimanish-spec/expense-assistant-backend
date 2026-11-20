package com.expenseassistant.ai;

import com.expenseassistant.dto.ExpenseExtractionResponse;
import com.expenseassistant.entity.Category;
import com.expenseassistant.exception.AiServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/** Turns Gemini's JSON text into validated DTOs. Invalid fields become null instead of failing the whole request. */
@Component
@RequiredArgsConstructor
public class GeminiResponseParser {

    private static final int MAX_MERCHANT_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 300;
    private static final int MAX_ITEMS = 50;
    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("dd.MM.yyyy"));

    private final ObjectMapper objectMapper;

    public ExpenseExtractionResponse parseExpense(String raw) {
        JsonNode node = readJson(raw);
        if (node.isArray() && !node.isEmpty()) {
            node = node.get(0);
        }
        if (!node.isObject()) {
            throw new AiServiceException("Gemini returned an unexpected response. Please try again.");
        }

        Category category = Category.parse(text(node, "category", 30));
        return new ExpenseExtractionResponse(
                text(node, "merchantName", MAX_MERCHANT_LENGTH),
                parseDate(text(node, "expenseDate", 30)),
                parseAmount(node.get("amount")),
                category != null ? category : Category.OTHER,
                text(node, "description", MAX_DESCRIPTION_LENGTH),
                parseItems(node.get("items")),
                null,
                null);
    }

    public List<String> parseSuggestions(String raw) {
        JsonNode node = readJson(raw);
        JsonNode array = node.isArray() ? node : node.get("suggestions");
        List<String> suggestions = new ArrayList<>();
        if (array != null && array.isArray()) {
            for (JsonNode item : array) {
                String value = item.asText("").strip();
                if (!value.isEmpty() && suggestions.size() < 5) {
                    suggestions.add(value);
                }
            }
        }
        if (suggestions.isEmpty()) {
            throw new AiServiceException("Gemini did not return any suggestions. Please try again.");
        }
        return suggestions;
    }

    private JsonNode readJson(String raw) {
        String cleaned = raw.strip();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceAll("^```(?:json)?\\s*", "").replaceAll("\\s*```$", "");
        }
        try {
            return objectMapper.readTree(cleaned);
        } catch (JsonProcessingException e) {
            throw new AiServiceException("Gemini returned invalid JSON. Please try again.");
        }
    }

    private String text(JsonNode node, String field, int maxLength) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        String text = value.asText("").strip();
        if (text.isEmpty() || text.equalsIgnoreCase("null")) {
            return null;
        }
        return text.length() > maxLength ? text.substring(0, maxLength) : text;
    }

    private LocalDate parseDate(String value) {
        if (value == null) {
            return null;
        }
        for (DateTimeFormatter format : DATE_FORMATS) {
            try {
                LocalDate date = LocalDate.parse(value, format);
                return date.getYear() >= 2000 ? date : null;
            } catch (DateTimeParseException ignored) {
                // try the next format
            }
        }
        return null;
    }

    private BigDecimal parseAmount(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        try {
            BigDecimal value = node.isNumber()
                    ? node.decimalValue()
                    : new BigDecimal(node.asText("").replaceAll("[^0-9.]", ""));
            return value.signum() > 0 ? value.setScale(2, RoundingMode.HALF_UP) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private List<String> parseItems(JsonNode node) {
        List<String> items = new ArrayList<>();
        if (node != null && node.isArray()) {
            for (JsonNode item : node) {
                String value = item.asText("").strip();
                if (!value.isEmpty() && items.size() < MAX_ITEMS) {
                    items.add(value.length() > 200 ? value.substring(0, 200) : value);
                }
            }
        }
        return items;
    }
}
