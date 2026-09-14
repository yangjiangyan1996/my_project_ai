package com.example.ai.exception;

public class AiToolException extends AiException {
    public AiToolException(String message) {
        super(message);
    }

    public AiToolException(String message, Throwable cause) {
        super(message, cause);
    }
}
