package com.expenseassistant.exception;

/** Raised when a receipt file cannot be stored, read or OCR'd. */
public class ReceiptProcessingException extends RuntimeException {
    public ReceiptProcessingException(String message) {
        super(message);
    }
}
