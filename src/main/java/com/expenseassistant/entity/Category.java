package com.expenseassistant.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Category {
    FOOD, TRAVEL, SHOPPING, BILLS, HEALTH, ENTERTAINMENT, EDUCATION, GROCERIES, OTHER;

    /** Returns null when the value is blank or not a known category. */
    @JsonCreator
    public static Category parse(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
