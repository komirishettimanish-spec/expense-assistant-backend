package com.expenseassistant.exception;

/** Raised when the Gemini API is unavailable, misconfigured or returns unusable output. */
public class AiServiceException extends RuntimeException {
    public AiServiceException(String message) {
        super(message);
    }
}
