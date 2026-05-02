package edu.ecommerce.core.exception;

public class InvalidOrderTransitionException extends RuntimeException {
    public InvalidOrderTransitionException(String message) {
        super(message);
    }

    public InvalidOrderTransitionException(String message, Throwable cause) {
        super(message, cause);
    }
}
