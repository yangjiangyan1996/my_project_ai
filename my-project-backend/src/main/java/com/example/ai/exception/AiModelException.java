package com.example.ai.exception;

public class AiModelException extends AiException {
    public AiModelException(String message) {
        super(message);
    }

    public AiModelException(String message, Throwable cause) {
        super(message, cause);
    }
}
