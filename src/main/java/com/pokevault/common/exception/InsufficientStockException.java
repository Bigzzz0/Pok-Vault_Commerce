package com.pokevault.common.exception;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String message) {
        super(message);
    }

    public InsufficientStockException(String message, Throwable cause) {
        super(message, cause);
    }

    public InsufficientStockException(String cardName, int requestedQty, int availableQty) {
        super(String.format("Insufficient stock for card '%s'. Requested: %d, Available: %d", 
                cardName, requestedQty, availableQty));
    }
}
